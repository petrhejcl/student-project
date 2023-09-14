package com.redhat.restdemo.servicetests;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.model.service.BookService;
import com.redhat.restdemo.model.service.BookServiceImpl;
import com.redhat.restdemo.testutils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.redhat.restdemo.testutils.TestUtils.resetTestDataIDs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookServiceTests {
    @InjectMocks
    private BookService bookService = new BookServiceImpl();

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorshipRepository authorshipRepository;

    @Mock
    private OwnershipRepository ownershipRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        resetTestDataIDs();
    }

    @Test
    void findAll() {
        bookService.findAll();
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findById() {
        // Prevents flaky tests, so there can not be same id twice
        int lowBound = 0;
        for (int i = 1; i < 5; i++) {
            int id = new Random().nextInt(2344) + lowBound + 1;
            bookService.findBookById(id);
            verify(bookRepository, times(1)).findById(id);
            lowBound = id;
        }
    }

    @Test
    void add() {
        for (Book book : TestData.books) {
            when(bookRepository.save(book)).thenReturn(book);
            assertThat(bookService.addBook(book), is(book));
            verify(bookRepository, times(1)).save(book);
        }
    }

    @Test
    void update() {
        int id = 1;

        for (Book book : TestData.books) {
            book = new Book(book);
            book.setId(id);
            when(bookRepository.findById(id)).thenReturn(Optional.of(book));
            Book newBook = new Book(12345L,"Random Book", 1900, "Thriller");
            when(bookRepository.save(book)).thenReturn(book);
            assertThat(bookService.updateBook(id, newBook), is(newBook));
            id++;
        }
    }

    @Test
    void delete() {
        int id = 1;

        for (Book book : TestData.books) {
            book.setId(id);

            when(bookRepository.findById(id)).thenReturn(Optional.of(book));

            Iterable<Ownership> ownerships = List.of(new Ownership(new Random().nextInt(100), book.getId()));
            when(ownershipRepository.findOwnershipsByBookId(book.getId())).thenReturn(ownerships);

            Iterable<Authorship> authorships = List.of(new Authorship(book.getId(), new Random().nextInt(100)));
            when(authorshipRepository.findAuthorshipsByBookId(book.getId())).thenReturn(authorships);

            assertThat(bookService.deleteBook(book.getId()), is(book));

            verify(ownershipRepository, times(1)).findOwnershipsByBookId(book.getId());
            verify(ownershipRepository, times(1)).deleteAll(ownerships);

            verify(authorshipRepository, times(1)).findAuthorshipsByBookId(book.getId());
            verify(authorshipRepository, times(1)).deleteAll(authorships);

            id++;
        }
    }
}
