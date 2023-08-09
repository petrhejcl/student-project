package com.redhat.restdemo.servicetests;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.service.AuthorshipService;
import com.redhat.restdemo.model.service.AuthorshipServiceImpl;
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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class AuthorshipServiceTests {
    @InjectMocks
    private AuthorshipService authorshipService = new AuthorshipServiceImpl();

    @Mock
    private AuthorshipRepository authorshipRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        resetTestDataIDs();
    }

    @Test
    void findAll() {
        authorshipService.findAll();
        verify(authorshipRepository, times(1)).findAll();
    }

    @Test
    void add() {
        int validAuthorId = new Random().nextInt(100);
        int validBookId = new Random().nextInt(100);
        int invalidAuthorId = new Random().nextInt(100) + 1000;
        int invalidBookId = new Random().nextInt(100) + 1000;

        Authorship validAuthorship = new Authorship(validBookId, validAuthorId);

        Authorship noBookIdAuthorship = new Authorship(null, validAuthorId);
        Authorship noAuthorIdAuthorship = new Authorship(validAuthorId, null);
        Authorship invalidBookIdAuthorship = new Authorship(invalidBookId, validAuthorId);
        Authorship invalidAuthorIdAuthorship = new Authorship(validBookId, invalidAuthorId);
        Authorship emptyAuthorship = new Authorship(null, null);
        List<Authorship> invalidAuthorships = List.of(
                noBookIdAuthorship, noAuthorIdAuthorship, emptyAuthorship,
                invalidBookIdAuthorship, invalidAuthorIdAuthorship);

        when(bookRepository.existsById(invalidBookId)).thenReturn(false);
        when(authorRepository.existsById(invalidAuthorId)).thenReturn(false);
        when(bookRepository.existsById(validBookId)).thenReturn(true);
        when(authorRepository.existsById(validAuthorId)).thenReturn(true);

        for (Authorship authorship : invalidAuthorships) {
            when(authorshipRepository.save(authorship)).thenReturn(authorship);
            assertThat(authorshipService.add(authorship), is(nullValue()));
            verify(authorshipRepository, times(0)).save(authorship);
        }

        when(authorshipRepository.save(validAuthorship)).thenReturn(validAuthorship);
        assertThat(authorshipService.add(validAuthorship), is(validAuthorship));
        verify(authorshipRepository, times(1)).save(validAuthorship);
    }

    @Test
    void delete() {
        int validAuthorshipId = new Random().nextInt(100);
        int invalidAuthorshipId = new Random().nextInt(100) + 1000;
        Authorship authorship = new Authorship(validAuthorshipId, 1234, 4321);

        when(authorshipRepository.findById(validAuthorshipId)).thenReturn(Optional.of(authorship));
        when(authorshipRepository.findById(invalidAuthorshipId)).thenReturn(Optional.empty());

        assertThat(authorshipService.delete(invalidAuthorshipId), is(nullValue()));
        verify(authorshipRepository, times(0)).delete(any());

        assertThat(authorshipService.delete(validAuthorshipId), is(authorship));
        verify(authorshipRepository, times(1)).delete(authorship);
    }
}

