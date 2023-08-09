package com.redhat.restdemo.servicetests;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.service.AuthorService;
import com.redhat.restdemo.model.service.AuthorServiceImpl;
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
        authorService.findAll();
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void findById() {
        // Prevents flaky tests, so there can not be same id twice
        int lowBound = 0;
        for (int i = 1; i < 5; i++) {
            int id = new Random().nextInt(2344) + lowBound + 1;
            authorService.findAuthorById(id);
            verify(authorRepository, times(1)).findById(id);
            lowBound = id;
        }
    }

    @Test
    void add() {
        for (Author author : TestData.authors) {
            when(authorRepository.save(author)).thenReturn(author);
            assertThat(authorService.addAuthor(author), is(author));
            verify(authorRepository, times(1)).save(author);
        }
    }

    @Test
    void update() {
        int id = 1;

        for (Author author : TestData.authors) {
            author = new Author(author);
            author.setId(id);
            when(authorRepository.findById(id)).thenReturn(Optional.of(author));
            Author newAuthor = new Author("Random", "Author", 1900);
            when(authorRepository.save(author)).thenReturn(author);
            assertThat(authorService.updateAuthor(id, newAuthor), is(newAuthor));
            id++;
        }
    }

    @Test
    void delete() {
        int id = 1;

        for (Author author : TestData.authors) {
            author.setId(id);

            when(authorRepository.findById(id)).thenReturn(Optional.of(author));

            Iterable<Authorship> authorships = List.of(new Authorship(new Random().nextInt(100), author.getId()));
            when(authorshipRepository.findAuthorshipsByAuthorId(author.getId())).thenReturn(authorships);

            assertThat(authorService.deleteAuthor(author.getId()), is(author));

            verify(authorshipRepository, times(1)).findAuthorshipsByAuthorId(author.getId());
            verify(authorshipRepository, times(1)).deleteAll(authorships);

            id++;
        }
    }
}
