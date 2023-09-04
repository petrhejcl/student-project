package com.redhat.restdemo.unittests;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.service.AuthorService;
import com.redhat.restdemo.model.service.AuthorServiceImpl;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static com.redhat.restdemo.utils.TestUtils.resetTestDataIDs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

class AuthorServiceTests {

    @InjectMocks
    private AuthorService authorService = new AuthorServiceImpl();

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorshipRepository authorshipRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        resetTestDataIDs();
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

    @Test
    void findById() {
        int currentId = 1;

        for (Author author : TestData.authors) {
            author.setId(currentId);
            when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));
            Optional<Author> foundAuthor = authorService.findAuthorById(currentId);
            assertThat(author, is(foundAuthor.get()));
            currentId++;
        }
    }

    @Test
    void add() {
        int idx = 1;
        for (Author author : TestData.authors) {
            when(authorRepository.save(author)).thenReturn(author);
            assertThat(authorService.addAuthor(author), is(author));
            verify(authorRepository, times(idx)).save(author);
            idx++;
        }
    }

    // Must add when for findById
    @Test
    void update() {
        int id = 1;

        for (Author author : TestData.authors) {
            author.setId(id);
            when(authorRepository.findById(id)).thenReturn(Optional.of(author));
            Author newAuthor = new Author("Random", "Author", 1900);
            when(authorRepository.save(author)).thenReturn(author);
            assertThat(authorService.updateAuthor(id, newAuthor), is(newAuthor));
            id++;
        }
    }
}
