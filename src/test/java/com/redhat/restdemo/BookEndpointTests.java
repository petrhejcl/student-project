package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BookEndpointTests extends EndpointTestTemplate {
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

    public void prepareBookSchema() {
        bookRepository.saveAll(TestData.books);
        assertThat(countIterable(bookRepository.findAll()), is((long) TestData.books.size()));
    }

    private List<Authorship> prepareAuthorshipSchema() {
        List<Authorship> authorships = new ArrayList<>();
        for (Map.Entry<Author, Book> entry : TestData.authorship.entrySet()) {
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

    @AfterEach
    public void clearRepos() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        libraryRepository.deleteAll();
        authorshipRepository.deleteAll();
        ownershipRepository.deleteAll();
    }

    @Test
    void testGetAllBooksEndpoint() throws IOException {
        prepareBookSchema();

        String bookUrl = createURLWithPort("/book");

        ResponseEntity<String> response = testRequests.get(bookUrl);

        List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(books.size(), is(TestData.books.size()));

        for (int i = 0; i < TestData.books.size(); i++) {
            assertThat(books.get(i).getIsbn(), is(TestData.books.get(i).getIsbn()));
            assertThat(books.get(i).getName(), is(TestData.books.get(i).getName()));
            assertThat(books.get(i).getYearOfRelease(), is(TestData.books.get(i).getYearOfRelease()));
            assertThat(books.get(i).getGenre(), is(TestData.books.get(i).getGenre()));
        }
    }

    @Test
    void testGetBookById() throws IOException {
        prepareBookSchema();

        String bookUrl = createURLWithPort("/book");

        ResponseEntity<String> response = testRequests.get(bookUrl);
        List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        for (Book book : books) {
            Integer id = book.getId();
            Book testBook = objectMapper.readValue(testRequests.get(bookUrl + "/" + id).getBody(), new TypeReference<>() {
            });
            assertThat(book, is(testBook));
        }

        int nonSenseId = new Random().nextInt(50000) + 100;
        ResponseEntity<String> nonSenseResponse = testRequests.get(bookUrl + "/" + nonSenseId);
        assert (nonSenseResponse.getStatusCode().is4xxClientError());
    }

    @Test
    void testGetBooksByGenre() throws IOException {
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
            assert(countIterable(genreBooks) == genres.get(genre).size());
            for (Book book : genreBooks) {
                assert(genres.get(genre).contains(book));
            }
        }
    }

    @Test
    void testGetBooksByAuthor() throws IOException {
        prepareAuthorshipSchema();

        String authorUrl = createURLWithPort("/book/author");

        for (Author author : authorRepository.findAll()) {
            Integer authorId = author.getId();
            ResponseEntity<String> response = testRequests.get(authorUrl+ "/" + authorId);
            assert(response.getStatusCode().is2xxSuccessful());
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(1));

            authorshipRepository.save(new Authorship(idCounter, authorId));
            response = testRequests.get(authorUrl + "/" + authorId);
            assert(response.getStatusCode().is2xxSuccessful());
            books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(2));
        }

        authorshipRepository.deleteAll();

        for (Author author : authorRepository.findAll()) {
            Integer authorId = author.getId();
            ResponseEntity<String> response = testRequests.get(authorUrl + "/" + authorId);
            assert(response.getStatusCode().is2xxSuccessful());
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(0));
        }
    }

    @Test
    void testGetBooksByLibrary() throws JsonProcessingException {
        List<Ownership> ownerships = prepareOwnershipSchema();

        String booksByLibraryurl = createURLWithPort("/book/library");

        for (Ownership ownership : ownerships) {
            Integer libraryId = ownership.getLibraryId();
            ResponseEntity<String> response = testRequests.get(booksByLibraryurl + "/" + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            int booksSize = books.size();
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book.getId(), ownership.getBookId())), is(true));

            Book newBook = bookRepository.save(new Book(new Random().nextInt(50000) + 100L, "Testing title", 1990, "Sci-Fi"));
            ownershipRepository.save(new Ownership(libraryId, newBook.getId()));
            response = testRequests.get(booksByLibraryurl + "/" + libraryId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(booksSize + 1));
            assertThat(books.stream()
                    .anyMatch(book -> Objects.equals(book.getId(), ownership.getBookId())), is(true));
        }
    }

    @Test
    void testAddBookEndpoint() throws IOException {
        String addBookUrl = createURLWithPort("/book/add");

        long size = countIterable(bookRepository.findAll());

        for (Book book : TestData.books) {
            ResponseEntity<String> response = testRequests.post(addBookUrl, book);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            long expectedSize = size + 1;
            assertThat(countIterable(bookRepository.findAll()), is(expectedSize));
            size = expectedSize;

            Book newBook = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(bookRepository.findById(newBook.getId()).get(), is(newBook));

            assertThat(newBook.getName(), is(book.getName()));
            assertThat(newBook.getIsbn(), is(book.getIsbn()));
            assertThat(newBook.getGenre(), is(book.getGenre()));
            assertThat(newBook.getYearOfRelease(), is(book.getYearOfRelease()));
        }
    }

    @Test
    void testUpdateBookEndpoint() throws IOException {
        prepareBookSchema();

        String bookUpdateUrl = createURLWithPort("/book/put");

        for (Book book : bookRepository.findAll()) {
            String newName = "New Book Title";
            testRequests.put(bookUpdateUrl + "/" + book.getId(), new Book(null, newName, null, null));
            Book updatedBook = objectMapper.readValue(testRequests.get(createURLWithPort("/book/" + book.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedBook.getId(), is(book.getId()));
            assertThat(updatedBook.getIsbn(), is(book.getIsbn()));
            assertThat(updatedBook.getName(), is(newName));
            assertThat(updatedBook.getYearOfRelease(), is(book.getYearOfRelease()));
            assertThat(updatedBook.getGenre(), is(book.getGenre()));
        }

        for (Book book : bookRepository.findAll()) {
            int newYearOfRelease = 2022;
            testRequests.put(bookUpdateUrl + "/" + book.getId(), new Book(null, null, newYearOfRelease, null));
            Book updatedBook = objectMapper.readValue(testRequests.get(createURLWithPort("/book/" + book.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedBook.getId(), is(book.getId()));
            assertThat(updatedBook.getIsbn(), is(book.getIsbn()));
            assertThat(updatedBook.getName(), is(book.getName()));
            assertThat(updatedBook.getYearOfRelease(), is(newYearOfRelease));
            assertThat(updatedBook.getGenre(), is(book.getGenre()));
        }

        for (Book book : bookRepository.findAll()) {
            String newGenre = "Sci-Fi";
            testRequests.put(bookUpdateUrl + "/" + book.getId(), new Book(null, null, null, newGenre));
            Book updatedBook = objectMapper.readValue(testRequests.get(createURLWithPort("/book/" + book.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedBook.getId(), is(book.getId()));
            assertThat(updatedBook.getIsbn(), is(book.getIsbn()));
            assertThat(updatedBook.getName(), is(book.getName()));
            assertThat(updatedBook.getYearOfRelease(), is(book.getYearOfRelease()));
            assertThat(updatedBook.getGenre(), is(newGenre));
        }

        for (Book book : bookRepository.findAll()) {
            String newName = "New Book Title";
            int newYearOfRelease = 2022;
            String newGenre = "Sci-Fi";
            testRequests.put(bookUpdateUrl + "/" + book.getId(), new Book(null, newName, newYearOfRelease, newGenre));
            Book updatedBook = objectMapper.readValue(testRequests.get(createURLWithPort("/book/" + book.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedBook.getId(), is(book.getId()));
            assertThat(updatedBook.getIsbn(), is(book.getIsbn()));
            assertThat(updatedBook.getName(), is(newName));
            assertThat(updatedBook.getYearOfRelease(), is(newYearOfRelease));
            assertThat(updatedBook.getGenre(), is(newGenre));
        }

        for (Book book : bookRepository.findAll()) {
            Long newIsbn = 37493234249L;
            testRequests.put(bookUpdateUrl + "/" + book.getId(), new Book(newIsbn, null, null, null));
            Book updatedBook = objectMapper.readValue(testRequests.get(createURLWithPort("/book/" + book.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedBook.getId(), is(book.getId()));
            assertThat(updatedBook.getIsbn(), is(newIsbn));
            assertThat(updatedBook.getName(), is(book.getName()));
            assertThat(updatedBook.getYearOfRelease(), is(book.getYearOfRelease()));
            assertThat(updatedBook.getGenre(), is(book.getGenre()));
        }

        ResponseEntity<String> nonSenseRequest;

        int nonSenseId = new Random().nextInt(50000) + 100;
        nonSenseRequest = testRequests.put(bookUpdateUrl + "/" + nonSenseId, new Book(4394732L, "Nonexistent Book", 2000, "Unknown"));
        assert (nonSenseRequest.getStatusCode().is4xxClientError());
    }

    @Test
    void testDeleteBookEndpoint() {
        prepareBookSchema();

        String bookDeleteUrl = createURLWithPort("/book/delete");

        Iterable<Book> books = bookRepository.findAll();

        Long booksCounter = countIterable(books);

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt();
            ResponseEntity<String> response = testRequests.delete(bookDeleteUrl + "/" + nonSenseId);
            assert (response.getStatusCode().is4xxClientError());
            assertThat(countIterable(bookRepository.findAll()), is(booksCounter));
        }

        for (Book book : books) {
            Integer bookId = book.getId();
            String deleteBookUrl = bookDeleteUrl + "/" + bookId;
            testRequests.delete(deleteBookUrl);
            booksCounter--;

            assertThat(booksCounter, is(countIterable(bookRepository.findAll())));
            ResponseEntity<String> getResponse = testRequests.get(createURLWithPort("/book/" + book.getId()));
            assert (getResponse.getStatusCode().is4xxClientError());
        }

        for (int i = 0; i <= idCounter; i++) {
            ResponseEntity<String> response = testRequests.delete(bookDeleteUrl + "/" + i);
            assert (response.getStatusCode().is4xxClientError());
            assertThat(countIterable(bookRepository.findAll()), is(booksCounter));
        }

        assertThat(countIterable(bookRepository.findAll()), is(0L));
    }
}
