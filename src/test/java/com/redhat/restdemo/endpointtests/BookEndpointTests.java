package com.redhat.restdemo.endpointtests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.restdemo.controllers.AuthorController;
import com.redhat.restdemo.model.entity.*;
import com.redhat.restdemo.model.repository.*;
import com.redhat.restdemo.testutils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static com.redhat.restdemo.testutils.TestUtils.countIterable;
import static com.redhat.restdemo.testutils.TestUtils.resetTestDataIDs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BookEndpointTests extends EndpointTestTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

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
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private AuthorshipRepository authorshipRepository;

    @Autowired
    private OwnershipRepository ownershipRepository;

    private String baseBookUrl;
    private String getBooksByAuthorUrl;
    private String getBooksByLibraryUrl;
    private String addBookUrl;
    private String putBookUrl;
    private String deleteBookUrl;

    @PostConstruct
    public void initializeUrls() {
        baseBookUrl = createURLWithPort("/book");
        getBooksByAuthorUrl = baseBookUrl + "/author/";
        getBooksByLibraryUrl = baseBookUrl + "/library/";
        addBookUrl = baseBookUrl + "/add";
        putBookUrl = baseBookUrl + "/put/";
        deleteBookUrl = baseBookUrl + "/delete/";
    }
    
    private void prepareBookSchema() {
        bookRepository.saveAll(new ArrayList<>(TestData.books));
        assertThat(countIterable(bookRepository.findAll()), is((long) TestData.books.size()));
    }

    private List<Authorship> prepareAuthorshipSchema() {
        List<Authorship> authorships = new ArrayList<>();
        for (Map.Entry<Author, Book> entry : new HashSet<>(TestData.authorship.entrySet())) {
            Integer authorId = authorRepository.save(entry.getKey()).getId();
            Integer bookId = bookRepository.save(entry.getValue()).getId();
            Authorship authorship = new Authorship(bookId, authorId);
            authorships.add(authorshipRepository.save(authorship));
        }
        assertThat(countIterable(authorshipRepository.findAll()), is((long) TestData.authorship.size()));
        return authorships;
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

    @BeforeEach
    public void clearRepos() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        libraryRepository.deleteAll();
        authorshipRepository.deleteAll();
        ownershipRepository.deleteAll();

        resetTestDataIDs();
    }

    @Test
    void shouldListAllBooks() throws IOException {
        prepareBookSchema();

        ResponseEntity<String> response = testRequests.get(baseBookUrl);

        List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(books.size(), is(TestData.books.size()));

        for (Book book : TestData.books) {
            assertThat(books.contains(book), is(true));
        }
    }

    @Test
    void shouldListBookById() throws IOException {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            Integer id = book.getId();
            ResponseEntity<String> response = testRequests.get(baseBookUrl + "/" + id);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book testBook = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(book, is(testBook));
        }

        int nonSenseId = new Random().nextInt(50000) + 100;
        ResponseEntity<String> nonSenseResponse = testRequests.get(baseBookUrl + "/" + nonSenseId);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
    }

    @Test
    void shouldListBooksByGenres() throws IOException {
        prepareBookSchema();

        HashMap<String, HashSet<Book>> genres = new HashMap<>();

        Iterable<Book> books = bookRepository.findAll();

        for (Book book : books) {
            String genre = book.getGenre();
            if (genres.containsKey(genre)) {
                genres.get(genre).add(book);
            } else {
                genres.put(genre, new HashSet<>(Set.of(book)));
            }
        }

        String genreUrl = createURLWithPort("/book/genre/");

        for (String genre : genres.keySet()) {
            Iterable<Book> genreBooks = objectMapper.readValue(testRequests.get(genreUrl + genre).getBody(), new TypeReference<>() {
            });
            assertThat(countIterable(genreBooks), is((long) genres.get(genre).size()));
            for (Book book : genreBooks) {
                assertThat(genres.get(genre).contains(book), is(true));
            }
        }
    }

    @Test
    void shouldListBooksByAuthor() throws IOException {
        List<Authorship> authorships = prepareAuthorshipSchema();

        for (Authorship authorship : authorships) {
            Integer authorId = authorship.getAuthorId();
            Integer bookId = authorship.getBookId();
            ResponseEntity<String> response = testRequests.get(getBooksByAuthorUrl + authorId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            int booksCount = books.size();
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book, bookRepository.findById(bookId).get())), is(true));

            Book testBook = bookRepository.save(new Book(84230L, "Test Book", 1900, "Sci-Fi"));
            authorshipRepository.save(new Authorship(testBook.getId(), authorId));
            response = testRequests.get(getBooksByAuthorUrl + authorId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(booksCount + 1));
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book, testBook)), is(true));
        }

        authorshipRepository.deleteAll();

        for (Author author : authorRepository.findAll()) {
            Integer authorId = author.getId();
            ResponseEntity<String> response = testRequests.get(getBooksByAuthorUrl + authorId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(0));
        }
    }

    @Test
    void shouldListBooksByLibrary() throws JsonProcessingException {
        List<Ownership> ownerships = prepareOwnershipSchema();

        for (Ownership ownership : ownerships) {
            Integer bookId = ownership.getBookId();
            Integer libraryId = ownership.getLibraryId();
            ResponseEntity<String> response = testRequests.get(getBooksByLibraryUrl + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            int booksSize = books.size();
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book, bookRepository.findById(bookId).get())), is(true));

            Book testBook = bookRepository.save(new Book(79472034L, "Testing title", 1990, "Sci-Fi"));
            ownershipRepository.save(new Ownership(libraryId, testBook.getId()));
            response = testRequests.get(getBooksByLibraryUrl + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(booksSize + 1));
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book, testBook)), is(true));
        }

        ownershipRepository.deleteAll();

        for (Library library : libraryRepository.findAll()) {
            Integer libraryId = library.getId();
            ResponseEntity<String> response = testRequests.get(getBooksByLibraryUrl + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(0));
        }
    }

    @Test
    void shouldAddBook() throws IOException {
        long bookCount = countIterable(bookRepository.findAll());

        for (Book book : TestData.books) {
            ResponseEntity<String> response = testRequests.post(addBookUrl, book);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book newBook = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(bookRepository.findById(newBook.getId()).get(), is(book));
            assertThat(countIterable(bookRepository.findAll()), is(bookCount + 1));
            bookCount++;
        }
    }

    @Test
    void shouldUpdateBookIsbn() {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            Long newIsbn = 123456789L;
            ResponseEntity<String> response = testRequests.put( putBookUrl + book.getId(), new Book(newIsbn, null, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book updatedBook = bookRepository.findById(book.getId()).get();
            Book referenceBook = new Book(newIsbn, book.getTitle(), book.getYearOfRelease(), book.getGenre());
            assertThat(updatedBook, is(referenceBook));
        }
    }

    @Test
    void shouldUpdateBookName() {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            String newName = "New Book Title";
            ResponseEntity<String> response = testRequests.put( putBookUrl + book.getId(), new Book(null, newName, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book updatedBook = bookRepository.findById(book.getId()).get();
            Book referenceBook = new Book(book.getIsbn(), newName, book.getYearOfRelease(), book.getGenre());
            assertThat(updatedBook, is(referenceBook));
        }
    }

    @Test
    void shouldUpdateBookYearOfRelease() {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            Integer newYearOfRelease = 1415;
            ResponseEntity<String> response = testRequests.put( putBookUrl + book.getId(), new Book(null, null, newYearOfRelease, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book updatedBook = bookRepository.findById(book.getId()).get();
            Book referenceBook = new Book(book.getIsbn(), book.getTitle(), newYearOfRelease, book.getGenre());
            assertThat(updatedBook, is(referenceBook));
        }
    }

    @Test
    void shouldUpdateBookGenre() {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            String newGenre = "Great Book";
            ResponseEntity<String> response = testRequests.put( putBookUrl + book.getId(), new Book(null, null, null, newGenre));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book updatedBook = bookRepository.findById(book.getId()).get();
            Book referenceBook = new Book(book.getIsbn(), book.getTitle(), book.getYearOfRelease(), newGenre);
            assertThat(updatedBook, is(referenceBook));
        }
    }

    @Test
    void shouldUpdateWholeBook() {
        prepareBookSchema();

        for (Book book : bookRepository.findAll()) {
            Long newIsbn = 99999999L;
            String newName = "New Book Title";
            Integer newYearOfRelease = 2022;
            String newGenre = "Sci-Fi";
            ResponseEntity<String> response = testRequests.put(putBookUrl + book.getId(), new Book(newIsbn, newName, newYearOfRelease, newGenre));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Book updatedBook = bookRepository.findById(book.getId()).get();
            Book referenceBook = new Book(newIsbn, newName, newYearOfRelease, newGenre);
            assertThat(updatedBook, is(referenceBook));
        }
    }

    @Test
    void shouldNotUpdateAnythingWhenTryingToUpdateInvalidId() {
        prepareBookSchema();

        Iterable<Book> beforeRequestBooks = bookRepository.findAll();

        Long nonSenseId = new Random().nextInt(50000) + 100L;
        ResponseEntity<String> nonSenseRequest = testRequests.put(putBookUrl + nonSenseId, new Book(4394732L, "Nonexistent Book", 2000, "Unknown"));
        assertThat(nonSenseRequest.getStatusCode().is4xxClientError(), is(true));

        Iterable<Book> afterRequestBooks = bookRepository.findAll();

        assertThat(beforeRequestBooks, is(afterRequestBooks));
    }

    @Test
    void shouldNotDeleteAnythingWhenTryingToDeleteInvalidId() {
        prepareBookSchema();

        Iterable<Book> books = bookRepository.findAll();

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt(50000) + 100;
            ResponseEntity<String> response = testRequests.delete(
                    deleteBookUrl + nonSenseId);
            assertThat(response.getStatusCode().is4xxClientError(), is(true));
            assertThat(books, is(bookRepository.findAll()));
        }
    }

    @Test
    void shouldDeleteBook() {
        prepareBookSchema();

        Iterable<Book> books = bookRepository.findAll();

        long booksCounter = countIterable(books);

        for (Book book : books) {
            Integer bookId = book.getId();
            ResponseEntity<String> response = testRequests.delete(deleteBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            booksCounter--;

            assertThat(booksCounter, is(countIterable(bookRepository.findAll())));
            assertThat(bookRepository.existsById(bookId), is(false));
        }
    }

    @Test
    void shouldDeleteAllConnectedAuthorshipsWhenDeletingBook() {
        List<Authorship> authorships = prepareAuthorshipSchema();

        for (Authorship authorship : authorships) {
            assertThat(authorshipRepository.existsById(authorship.getId()), is(true));
            Integer bookId = authorship.getBookId();
            testRequests.delete(deleteBookUrl + bookId);
            assertThat(authorshipRepository.existsById(authorship.getId()), is(false));
        }
    }

    @Test
    void shouldDeleteAllConnectedOwnershipsWhenDeletingBook() {
        List<Ownership> ownerships = prepareOwnershipSchema();

        for (Ownership ownership : ownerships) {
            assertThat(ownershipRepository.existsById(ownership.getId()), is(true));
            Integer bookId = ownership.getBookId();
            testRequests.delete(deleteBookUrl + bookId);
            assertThat(ownershipRepository.existsById(ownership.getId()), is(false));
        }
    }
}
