package com.redhat.restdemo.unittests;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.service.AuthorService;
import com.redhat.restdemo.model.service.AuthorServiceImpl;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

class AuthorServiceTest {

    @InjectMocks
    private AuthorService authorService = new AuthorServiceImpl();

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorshipRepository authorshipRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findAll() {
        when(authorRepository.findAll()).thenReturn(TestData.authors);

        Iterable<Author> authors = authorService.findAll();

        assertThat(countIterable(authors), is((long) TestData.authors.size()));

        for (Author author : authors) {
            assertTrue(TestData.authors.contains(author));
        }
    }
}
