package com.redhat.restdemo.unittests;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.model.service.AuthorService;
import com.redhat.restdemo.model.service.AuthorServiceImpl;
import com.redhat.restdemo.model.service.BookService;
import com.redhat.restdemo.model.service.BookServiceImpl;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
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
    }

    @Test
    void findAll() {
        when(bookRepository.findAll()).thenReturn(TestData.books);

        Iterable<Book> books = bookService.findAll();

        assertThat(countIterable(books), is((long) TestData.books.size()));

        for (Book book : books) {
            assertTrue(TestData.books.contains(book));
        }
    }
}
