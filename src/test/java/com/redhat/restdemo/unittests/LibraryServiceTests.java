package com.redhat.restdemo.unittests;

import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.model.service.BookService;
import com.redhat.restdemo.model.service.BookServiceImpl;
import com.redhat.restdemo.model.service.LibraryService;
import com.redhat.restdemo.model.service.LibraryServiceImpl;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Random;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static com.redhat.restdemo.utils.TestUtils.resetTestDataIDs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class LibraryServiceTests {
    @InjectMocks
    private LibraryService libraryService = new LibraryServiceImpl();

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private OwnershipRepository ownershipRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        resetTestDataIDs();
    }

    @Test
    void findAll() {
        libraryService.findAll();
        verify(libraryRepository, times(1)).findAll();
    }

    @Test
    void findById() {
        // Prevents flaky tests, so there can not be same id twice
        int lowBound = 0;
        for (int i = 1; i < 5; i++) {
            int id = new Random().nextInt(2344) + lowBound + 1;
            libraryService.findLibraryById(id);
            verify(libraryRepository, times(1)).findById(id);
            lowBound = id;
        }
    }

    @Test
    void add() {
        for (Library library : TestData.libraries) {
            when(libraryRepository.save(library)).thenReturn(library);
            assertThat(libraryService.addLibrary(library), is(library));
            verify(libraryRepository, times(1)).save(library);
        }
    }
}
