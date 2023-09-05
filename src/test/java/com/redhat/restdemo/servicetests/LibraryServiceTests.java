package com.redhat.restdemo.servicetests;

import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.model.service.LibraryService;
import com.redhat.restdemo.model.service.LibraryServiceImpl;
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

    @Test
    void update() {
        int id = 1;

        for (Library library : TestData.libraries) {
            library = new Library(library);
            library.setId(id);
            when(libraryRepository.findById(id)).thenReturn(Optional.of(library));
            Library newLibrary = new Library("Random Library", "Random City", "Random Street", 111, "Random Desc");
            when(libraryRepository.save(library)).thenReturn(library);
            assertThat(libraryService.updateLibrary(id, newLibrary), is(newLibrary));
            id++;
        }
    }

    @Test
    void delete() {
        int id = 1;

        for (Library library : TestData.libraries) {
            library.setId(id);

            when(libraryRepository.findById(id)).thenReturn(Optional.of(library));

            Iterable<Ownership> ownerships = List.of(new Ownership(library.getId(), new Random().nextInt(100)));
            when(ownershipRepository.findOwnershipsByLibraryId(library.getId())).thenReturn(ownerships);

            assertThat(libraryService.deleteLibrary(library.getId()), is(library));

            verify(ownershipRepository, times(1)).findOwnershipsByLibraryId(library.getId());
            verify(ownershipRepository, times(1)).deleteAll(ownerships);

            id++;
        }
    }
}
