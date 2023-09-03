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

import static com.redhat.restdemo.utils.TestUtils.countIterable;
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
    }

    @Test
    void findAll() {
        when(libraryRepository.findAll()).thenReturn(TestData.libraries);

        Iterable<Library> libraries = libraryService.findAll();

        assertThat(countIterable(libraries), is((long) TestData.libraries.size()));

        for (Library library : libraries) {
            assertTrue(TestData.libraries.contains(library));
        }
    }

    @Test
    void findById() {
        int currentId = 1;

        for (Library library: TestData.libraries) {
            library.setId(currentId);
            when(libraryRepository.findById(library.getId())).thenReturn(Optional.of(library));
            Optional<Library> foundLibrary = libraryService.findLibraryById(currentId);
            assertThat(library, is(foundLibrary.get()));
            currentId++;
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