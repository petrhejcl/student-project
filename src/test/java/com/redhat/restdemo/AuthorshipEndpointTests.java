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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AuthorshipEndpointTests extends EndpointTestTemplate {
    @Autowired
    private AuthorshipRepository authorshipRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    public void prepareAuthorBookSchemas() throws IOException {
        prepareSchema(authorRepository, createURLWithPort("/author/add"), TestData.authors);
        prepareSchema(bookRepository, createURLWithPort("/book/add"), TestData.books);

        LinkedList<Authorship> authorship = new LinkedList<>();

        Iterable<Author> authors = authorRepository.findAll();

        Iterable<Book> books = bookRepository.findAll();

        for (Author author : authors) {
            String surname = author.getSurname();
            for (Book book : books) {
                Long isbn = book.getIsbn();
                if ((surname.equals("Rowling") && isbn == 9780747532743L) ||
                        (surname.equals("Orwell") && isbn == 9780451524935L) ||
                        (surname.equals("Austen") && isbn == 9780141439518L) ||
                        (surname.equals("Hemingway") && isbn == 9780684801223L) ||
                        (surname.equals("Angelou") && isbn == 9780345514400L) ||
                        (surname.equals("Bukowski") && isbn == 9780876857632L)) {
                    authorship.add(new Authorship(author.getId(), book.getId()));
                }
            }
        }
        prepareSchema(authorshipRepository, createURLWithPort("/authorship/add"), authorship);
    }

    @Test
    public void testGetAuthorshipEndpoint() throws IOException {
        prepareAuthorBookSchemas();

        String authorshipUrl = createURLWithPort("/authorship");

        ResponseEntity<String> response = testRequests.get(authorshipUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Authorship> bookAuthorship = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(bookAuthorship.size(), is(6));

        for (Authorship authorship : bookAuthorship) {
            Integer authorId = authorship.getAuthorId();
            Integer bookId = authorship.getBookId();

            Optional<Author> author = authorRepository.findById(authorId);
            assert (author.isPresent());
            Optional<Book> book = bookRepository.findById(bookId);
            assert (book.isPresent());

            switch (author.get().getSurname()) {
                case "Rowling":
                    assertThat(book.get().getIsbn(), is(9780747532743L));
                    break;
                case "Orwell":
                    assertThat(book.get().getIsbn(), is(9780451524935L));
                    break;
                case "Austen":
                    assertThat(book.get().getIsbn(), is(9780141439518L));
                    break;
                case "Hemingway":
                    assertThat(book.get().getIsbn(), is(9780684801223L));
                    break;
                case "Angelou":
                    assertThat(book.get().getIsbn(), is(9780345514400L));
                    break;
                case "Bukowski":
                    assertThat(book.get().getIsbn(), is(9780876857632L));
                    break;
                default:
                    throw new RuntimeException("Author with given surname should not be there");
            }
        }
    }

    @Test
    public void testAddAuthorshipEndpoint() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String addAuthorshipUrl = createURLWithPort("/authorship/add");

        Book testBook1 = new Book(1L, "Test 1", 2000, "Test genre 1");
        bookRepository.save(testBook1);
        Book testBook2 = new Book(2L, "Test 2", 1900, "Test genre 3");
        bookRepository.save(testBook2);
        Book testBook3 = new Book(3L, "Test 3", 1800, "Test genre 3");
        bookRepository.save(testBook3);

        Author testAuthor1 = new Author("John", "Doe", 2010);
        authorRepository.save(testAuthor1);
        Author testAuthor2 = new Author("Pepa", "Novak", 1910);
        authorRepository.save(testAuthor2);
        Author testAuthor3 = new Author("Pablo", "Neruda", 1810);
        authorRepository.save(testAuthor3);

        long authorshipCounter = 0L;

        assertThat(authorshipRepository.count(), is(authorshipCounter));

        Iterable<Author> authors = authorRepository.findAll();
        Iterable<Book> books = bookRepository.findAll();

        for (Author author : authors) {
            for (Book book : books) {
                Authorship newAuthorship = new Authorship(author.getId(), book.getId());
                ResponseEntity<String> response = testRequests.post(addAuthorshipUrl, newAuthorship);
                assert(response.getStatusCode().is2xxSuccessful());
                authorshipCounter++;
                assertThat(authorshipCounter, is(authorshipRepository.count()));
                Authorship authorship = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                });
                assertThat(authorshipRepository.findById(authorship.getId()).get().getAuthorId(), is(newAuthorship.getAuthorId()));
                assertThat(authorshipRepository.findById(authorship.getId()).get().getBookId(), is(newAuthorship.getBookId()));
            }
        }

        Authorship nonSenseAuthorship;
        ResponseEntity<String> nonSenseResponse;

        int nonSenseAuthorId = new Random().nextInt(50000) + 100;
        nonSenseAuthorship = new Authorship(nonSenseAuthorId, books.iterator().next().getId());
        nonSenseResponse = testRequests.post(addAuthorshipUrl, nonSenseAuthorship);
        assert(nonSenseResponse.getStatusCode().is4xxClientError());

        int nonSenseBookId = new Random().nextInt(50000) + 100;
        nonSenseAuthorship = new Authorship(authors.iterator().next().getId(), nonSenseBookId);
        nonSenseResponse = testRequests.post(addAuthorshipUrl, nonSenseAuthorship);
        assert(nonSenseResponse.getStatusCode().is4xxClientError());
    }

    @Test
    public void testDeleteAuthorshipEndpoint() throws IOException {
        //TODO
    }

}
