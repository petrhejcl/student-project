package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LibraryEndpointTests extends EndpointTestTemplate {
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

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OwnershipRepository ownershipRepository;

    private String baseLibraryUrl;
    private String getLibrariesByBookUrl;
    private String addLibraryUrl;
    private String putLibraryUrl;
    private String deleteLibraryUrl;

    @PostConstruct
    public void initializeUrls() {
        baseLibraryUrl = createURLWithPort("/library");
        getLibrariesByBookUrl = baseLibraryUrl + "/book/";
        addLibraryUrl = baseLibraryUrl + "/add";
        putLibraryUrl = baseLibraryUrl + "/put/";
        deleteLibraryUrl = baseLibraryUrl + "/delete/";
    }

    private void prepareLibraryScheme() {
        libraryRepository.saveAll(TestData.libraries);
        assertThat(countIterable(libraryRepository.findAll()), is((long) TestData.books.size()));
    }

    private List<Ownership> prepareOwnershipSchema() {
        List<Ownership> ownerships = new ArrayList<>();
        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            Ownership ownership = new Ownership(libraryId, bookId);
            ownerships.add(ownershipRepository.save(ownership));
        }
        assertThat(countIterable(ownershipRepository.findAll()), is((long) TestData.ownership.size()));
        return ownerships;
    }

    @AfterEach
    void clearRepos() {
        libraryRepository.deleteAll();
        bookRepository.deleteAll();
        ownershipRepository.deleteAll();

        resetTestDataIDs();
    }

    @Test
    void shouldListAllLibraries() throws JsonProcessingException {
        prepareLibraryScheme();

        ResponseEntity<String> response = testRequests.get(baseLibraryUrl);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(libraries.size(), is(TestData.libraries.size()));

        for (Library library : TestData.libraries) {
            assertThat(libraries.contains(library), is(true));
        }
    }

    @Test
    void shouldListLibraryById() throws JsonProcessingException {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            Integer id = library.getId();
            ResponseEntity<String> response = testRequests.get(baseLibraryUrl + "/" + id);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library testLibrary = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(library, is(testLibrary));
        }

        int nonSenseId = new Random().nextInt(50000) + 100;
        ResponseEntity<String> nonSenseResponse = testRequests.get(baseLibraryUrl + "/" + nonSenseId);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
    }

    @Test
    void shouldListLibrariesByBook() throws JsonProcessingException {
        List<Ownership> ownerships = prepareOwnershipSchema();

        for (Ownership ownership : ownerships) {
            Integer bookId = ownership.getBookId();
            Integer libraryId = ownership.getLibraryId();
            ResponseEntity<String> response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            int librariesCount = libraries.size();
            assertThat(libraries.stream()
                    .anyMatch(library -> Objects.equals(library, libraryRepository.findById(libraryId).get())), is(true));

            Library testLibrary = libraryRepository.save(new Library("Test Library", "Brno", "Sumavska", 123,"Great Library"));
            ownershipRepository.save(new Ownership(testLibrary.getId(), bookId));
            response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(libraries.size(), is(librariesCount + 1));
            assertThat(libraries.stream()
                    .anyMatch(library -> Objects.equals(library, testLibrary)), is(true));
        }

        ownershipRepository.deleteAll();

        for (Book book : bookRepository.findAll()) {
            Integer bookId = book.getId();
            ResponseEntity<String> response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(0));
        }
    }

    @Test
    void shouldAddLibrary() throws IOException {
        long libraryCount = countIterable(libraryRepository.findAll());

        for (Library library : TestData.libraries) {
            ResponseEntity<String> response = testRequests.post(addLibraryUrl, library);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library newLibrary = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(libraryRepository.findById(newLibrary.getId()).get(), is(library));
            assertThat(countIterable(libraryRepository.findAll()), is(libraryCount + 1));
            libraryCount++;
        }
    }

    @Test
    void shouldUpdateLibraryName() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newName = "Test Library";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(newName, null, null, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(newName, library.getCity(), library.getStreet(), library.getStreetNumber(), library.getDescription());
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldUpdateLibraryCity() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newCity = "Test city";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(null, newCity, null, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(library.getName(), newCity, library.getStreet(), library.getStreetNumber(), library.getDescription());
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldUpdateLibraryStreet() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newStreet = "Test street";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(null, null, newStreet, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(library.getName(), library.getCity(), newStreet, library.getStreetNumber(), library.getDescription());
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldUpdateLibraryStreetNumber() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            Integer newStreetNumber = 999;
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(null, null, null, newStreetNumber, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(library.getName(), library.getCity(), library.getStreet(), newStreetNumber, library.getDescription());
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldUpdateLibraryDescription() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newDescription = "Great library for books";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(null, null, null, null, newDescription));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(library.getName(), library.getCity(), library.getStreet(), library.getStreetNumber(), newDescription);
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldUpdateWholeLibrary() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newName = "Knihovna Rehore Samsy";
            String newCity = "Praha";
            String newStreet = "Stare Mesto";
            Integer newStreetNumber = 111;
            String newDescription = "Knihovnicka";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(newName, newCity, newStreet, newStreetNumber, newDescription));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(newName, newCity, newStreet, newStreetNumber, newDescription);
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    @Test
    void shouldNotUpdateAnythingWhenTryingToUpdateInvalidId() {
        prepareLibraryScheme();

        Iterable<Library> beforeRequestLibraries = libraryRepository.findAll();

        Long nonSenseId = new Random().nextInt(50000) + 100L;
        ResponseEntity<String >nonSenseRequest = testRequests.put(putLibraryUrl + nonSenseId, new Library("New Library", "New City", "New Street", 123, "New description"));
        assertThat(nonSenseRequest.getStatusCode().is4xxClientError(), is(true));

        Iterable<Library> afterRequestLibraries = libraryRepository.findAll();

        assertThat(afterRequestLibraries, is(beforeRequestLibraries));
    }

    @Test
    void shouldNotDeleteAnythingWhenTryingToDeleteInvalidId() {
        prepareLibraryScheme();

        Iterable<Library> libraries = libraryRepository.findAll();

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt(50000) + 100;
            ResponseEntity<String> response = testRequests.delete(
                    deleteLibraryUrl + nonSenseId);
            assertThat(response.getStatusCode().is4xxClientError(), is(true));
            assertThat(libraries, is(libraryRepository.findAll()));
        }
    }

    @Test
    void shouldDeleteLibrary() {
        prepareLibraryScheme();

        Iterable<Library> libraries = libraryRepository.findAll();

        long libraryCounter = countIterable(libraries);

        for (Library library : libraries) {
            Integer libraryId = library.getId();
            ResponseEntity<String> response = testRequests.delete(deleteLibraryUrl + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            libraryCounter--;

            assertThat(libraryCounter, is(countIterable(libraryRepository.findAll())));
            assertThat(libraryRepository.existsById(libraryId), is(false));
        }
    }

    @Test
    void shouldDeleteAllConnectedOwnershipsWhenDeletingLibrary() {
        List<Ownership> ownerships = prepareOwnershipSchema();

        for (Ownership ownership : ownerships) {
            assertThat(ownershipRepository.existsById(ownership.getId()), is(true));
            Integer libraryId = ownership.getLibraryId();
            testRequests.delete(deleteLibraryUrl + libraryId);
            assertThat(ownershipRepository.existsById(ownership.getId()), is(false));
        }
    }
}
