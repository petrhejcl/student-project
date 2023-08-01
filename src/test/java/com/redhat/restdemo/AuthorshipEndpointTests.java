package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.StreamSupport;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AuthorshipEndpointTests extends EndpointTestTemplate {
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

    private String baseAuthorshipUrl;
    private String addAuthorshipUrl;
    private String deleteAuthorshipUrl;

    @PostConstruct
    public void initializeUrls() {
        baseAuthorshipUrl = createURLWithPort("/authorship");
        addAuthorshipUrl = baseAuthorshipUrl + "/add";
        deleteAuthorshipUrl = baseAuthorshipUrl + "/delete/";
    }

    @Autowired
    private AuthorshipRepository authorshipRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private void prepareAuthorBookSchemas() {
        for (Map.Entry<Author, Book> entry : TestData.authorship.entrySet()) {
            Integer authorId = authorRepository.save(entry.getKey()).getId();
            Integer bookId = bookRepository.save(entry.getValue()).getId();
            authorshipRepository.save(new Authorship(bookId, authorId));
        }
    }

    @AfterEach
    public void clearRepository() {
        authorshipRepository.deleteAll();
        authorRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void shouldListAllAuthorships() throws IOException {
        prepareAuthorBookSchemas();

        ResponseEntity<String> response = testRequests.get(baseAuthorshipUrl);

        List<Authorship> authorships = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(authorships.size(), is(TestData.authorship.size()));

        for (Authorship authorship : authorships) {
            Integer authorId = authorship.getAuthorId();
            Integer bookId = authorship.getBookId();

            Optional<Author> author = authorRepository.findById(authorId);
            assertThat(author.isPresent(), is(true));
            Optional<Book> book = bookRepository.findById(bookId);
            assertThat(book.isPresent(), is(true));

            assertThat(TestData.authorship.get(author.get()), is(book.get()));
        }
    }

    @Test
    void shouldAddNewAuthorship() {
        Long authorshipCounter = countIterable(authorshipRepository.findAll());

        for (Map.Entry<Author, Book> entry : TestData.authorship.entrySet()) {
            Integer authorId = authorRepository.save(entry.getKey()).getId();
            Integer bookId = bookRepository.save(entry.getValue()).getId();
            assertThat(StreamSupport
                    .stream(bookRepository.findBooksByAuthor(authorId).spliterator(), false)
                    .anyMatch(book -> book.getId().equals(bookId)), is(false));

            ResponseEntity<String> response = testRequests.post(addAuthorshipUrl, (new Authorship(authorId, bookId)));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(authorshipCounter + 1, is(countIterable(authorshipRepository.findAll())));
            authorshipCounter++;
            assertThat(StreamSupport
                    .stream(bookRepository.findBooksByAuthor(authorId).spliterator(), false)
                    .anyMatch(book -> book.getId().equals(bookId)), is(true));
        }
    }

    @Test
    void shouldNotAddAnythingWhenTryingToAddAuthorshipToNonExistingAuthor() {
        Long authorshipCount = countIterable(authorshipRepository.findAll());

        for (int i = 0; i < 3; i++) {
            Author justSomeRandomAuthor = new Author("Random author", "" + i, 1730 + i);
            authorRepository.save(justSomeRandomAuthor);
        }

        int nonSenseAuthorId = new Random().nextInt(50000) + 1000;
        assertThat(authorRepository.existsById(nonSenseAuthorId), is(false));
        Book testBook = bookRepository.save(new Book(111L, "Test book", 1900, "Great book"));
        Authorship nonSenseAuthorship = new Authorship(nonSenseAuthorId, testBook.getId());
        ResponseEntity<String> nonSenseResponse = testRequests.post(addAuthorshipUrl, nonSenseAuthorship);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
        assertThat(authorshipCount, is(countIterable(authorshipRepository.findAll())));

    }

    @Test
    void shouldNotAddAnythingWhenTryingToAddAuthorshipToNonExistingBook() {
        Long authorshipCount = countIterable(authorshipRepository.findAll());

        for (int i = 0; i < 3; i++) {
            Book justSomeRandomBook = new Book((long) i, "Test book", 1900, "Sci-Fi");
            bookRepository.save(justSomeRandomBook);
        }

        int nonSenseBookId = new Random().nextInt(50000) + 1000;
        assertThat(bookRepository.existsById(nonSenseBookId), is(false));
        Author testAuthor = authorRepository.save(new Author("Test", "Author", 1900));
        Authorship nonSenseAuthorship = new Authorship(testAuthor.getId(), nonSenseBookId);
        ResponseEntity<String> nonSenseResponse = testRequests.post(addAuthorshipUrl, nonSenseAuthorship);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
        assertThat(authorshipCount, is(countIterable(authorshipRepository.findAll())));
    }

    @Test
    void shouldNotDeleteAnythingWhenTryingToDeleteInvalidId() {
        prepareAuthorBookSchemas();

        Iterable<Authorship> authorships = authorshipRepository.findAll();

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt(50000) + 1000;
            ResponseEntity<String> response = testRequests.delete(
                    deleteAuthorshipUrl + nonSenseId);
            assertThat(response.getStatusCode().is4xxClientError(), is(true));
            assertThat(authorships, is(authorshipRepository.findAll()));
        }
    }

    @Test
    void shouldDeleteAuthorship() {
        prepareAuthorBookSchemas();

        Iterable<Authorship> authorships = authorshipRepository.findAll();

        Long authorshipCounter = countIterable(authorships);

        for (Authorship authorship : authorships) {
            Integer authorshipId = authorship.getId();
            assertThat(StreamSupport
                    .stream(authorshipRepository.findAll().spliterator(), false)
                    .anyMatch(as -> as.equals(authorship)), is(true));

            ResponseEntity<String> response = testRequests.delete(deleteAuthorshipUrl + authorshipId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(countIterable(authorshipRepository.findAll()), is(authorshipCounter - 1));
            authorshipCounter--;
            assertThat(StreamSupport
                    .stream(authorshipRepository.findAll().spliterator(), false)
                    .anyMatch(as -> as.equals(authorship)), is(false));
        }
    }
}
