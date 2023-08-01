package com.redhat.restdemo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.*;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.management.ObjectName;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OwnershipEndpointTests extends EndpointTestTemplate {
    @Container
    private static PostgreSQLContainer postgresqlContainer;

    static {
        postgresqlContainer = new PostgreSQLContainer("postgres:14")
                .withDatabaseName("postgres")
                .withUsername("compose-postgres")
                .withPassword("compose-postgres");
        postgresqlContainer.start();
    }

    @DynamicPropertySource
    protected static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private String baseOwnershipUrl;
    private String addOwnershipUrl;
    private String deleteOwnershipUrl;

    @PostConstruct
    public void initializeUrls() {
        baseOwnershipUrl = createURLWithPort("/ownership");
        addOwnershipUrl = baseOwnershipUrl + "/add";
        deleteOwnershipUrl = baseOwnershipUrl + "/delete/";
    }

    @Autowired
    private OwnershipRepository ownershipRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private BookRepository bookRepository;

    private void prepareLibraryBookSchema() {
        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            ownershipRepository.save(new Ownership(libraryId, bookId));
        }
    }

    @BeforeEach
    public void cleanRepos() {
        ownershipRepository.deleteAll();
        bookRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void shouldListAllOwnerships() throws JsonProcessingException {
        prepareLibraryBookSchema();

        ResponseEntity<String> response = testRequests.get(baseOwnershipUrl);
        List<Ownership> bookOwnership = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(bookOwnership.size(), is(TestData.ownership.size()));

        for (Ownership ownership : bookOwnership) {
            Integer bookId = ownership.getBookId();
            Integer libraryId = ownership.getLibraryId();

            Optional<Book> book = bookRepository.findById(bookId);
            assertThat(book.isPresent(), is(true));
            Optional<Library> library = libraryRepository.findById(libraryId);
            assertThat(library.isPresent(), is(true));

            assertThat(TestData.ownership.get(book.get()), is(library.get()));
        }
    }

    @Test
    void shouldAddNewOwnership() {
        Long ownershipCounter = countIterable(ownershipRepository.findAll());

        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            assertThat(StreamSupport
                    .stream(bookRepository.findBooksByLibrary(libraryId).spliterator(), false)
                    .anyMatch(book -> book.getId().equals(bookId)), is(false));

            ResponseEntity<String> response = testRequests.post(addOwnershipUrl, (new Ownership(libraryId, bookId)));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(ownershipCounter + 1, is(countIterable(ownershipRepository.findAll())));
            ownershipCounter++;
            assertThat(StreamSupport
                    .stream(bookRepository.findBooksByLibrary(libraryId).spliterator(), false)
                    .anyMatch(book -> book.getId().equals(bookId)), is(true));
        }
    }

    @Test
    void shouldNotAddAnythingWhenTryingToAddOwnershipToNonExistingLibrary() {
        Long ownershipCount = countIterable(ownershipRepository.findAll());

        for (int i = 0; i < 3; i++) {
            Library justSomeRandomLibrary = new Library("Random Library", "Random City", "Random street", i, "Just random lib");
            libraryRepository.save(justSomeRandomLibrary);
        }

        int nonSenseLibraryId = new Random().nextInt(50000) + 1000;
        assertThat(libraryRepository.existsById(nonSenseLibraryId), is(false));
        Book testBook = bookRepository.save(new Book(111L, "Test book", 1900, "Great book"));
        Ownership nonSenseOwnership = new Ownership(nonSenseLibraryId, testBook.getId());
        ResponseEntity<String> nonSenseResponse = testRequests.post(addOwnershipUrl, nonSenseOwnership);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
        assertThat(ownershipCount, is(countIterable(ownershipRepository.findAll())));
    }

    @Test
    void shouldNotAddAnythingWhenTryingToAddOwnershipToNonExistingBook() {
        Long ownershipCount = countIterable(ownershipRepository.findAll());

        for (int i = 0; i < 3; i++) {
            Book justSomeRandomBook = new Book((long) i, "Test book", 1900, "Sci-Fi");
            bookRepository.save(justSomeRandomBook);
        }

        int nonSenseBookId = new Random().nextInt(50000) + 1000;
        assertThat(bookRepository.existsById(nonSenseBookId), is(false));
        Library testLibrary = libraryRepository.save(new Library("Test Library", "Test City", "Test street", 999, "Test description"));
        Ownership nonSenseOwnership = new Ownership(testLibrary.getId(), nonSenseBookId);
        ResponseEntity<String> nonSenseResponse = testRequests.post(addOwnershipUrl, nonSenseOwnership);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
        assertThat(ownershipCount, is(countIterable(ownershipRepository.findAll())));
    }

    @Test
    void shouldNotDeleteAnythingWhenTryingToDeleteInvalidId() {
        prepareLibraryBookSchema();

        Iterable<Ownership> ownerships = ownershipRepository.findAll();

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt(50000) + 1000;
            ResponseEntity<String> response = testRequests.delete(
                    deleteOwnershipUrl + nonSenseId);
            assertThat(response.getStatusCode().is4xxClientError(), is(true));
            assertThat(ownerships, is(ownershipRepository.findAll()));
        }
    }

    @Test
    void shouldDeleteOwnership() {
        prepareLibraryBookSchema();

        Iterable<Ownership> ownerships = ownershipRepository.findAll();

        Long ownershipCounter = countIterable(ownerships);

        for (Ownership ownership : ownerships) {
            Integer ownershipId = ownership.getId();
            assertThat(StreamSupport
                    .stream(ownershipRepository.findAll().spliterator(), false)
                    .anyMatch(os -> os.equals(ownership)), is(true));

            ResponseEntity<String> response = testRequests.delete(deleteOwnershipUrl + ownershipId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(countIterable(ownershipRepository.findAll()), is(ownershipCounter - 1));
            ownershipCounter--;
            assertThat(StreamSupport
                    .stream(ownershipRepository.findAll().spliterator(), false)
                    .anyMatch(os -> os.equals(ownership)), is(false));
        }
    }
}
