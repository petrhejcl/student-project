package com.redhat.restdemo.servicetests;

import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.model.service.OwnershipService;
import com.redhat.restdemo.model.service.OwnershipServiceImpl;
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

public class OwnershipServiceTests {
    @InjectMocks
    private OwnershipService ownershipService = new OwnershipServiceImpl();

    @Mock
    private OwnershipRepository ownershipRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        resetTestDataIDs();
    }

    @Test
    void findAll() {
        ownershipService.findAll();
        verify(ownershipRepository, times(1)).findAll();
    }

    @Test
    void add() {
        int validLibraryId = new Random().nextInt(100);
        int validBookId = new Random().nextInt(100);
        int invalidLibraryId = new Random().nextInt(100) + 1000;
        int invalidBookId = new Random().nextInt(100) + 1000;

        Ownership validOwnership = new Ownership(validLibraryId, validBookId);

        Ownership noBookIdOwnership = new Ownership(validLibraryId, null);
        Ownership noLibraryIdOwnership = new Ownership(null, validLibraryId);
        Ownership invalidBookIdOwnership = new Ownership(validLibraryId, invalidBookId);
        Ownership invalidLibraryIdOwnership = new Ownership(invalidLibraryId, validBookId);
        Ownership emptyOwnership = new Ownership(null, null);
        List<Ownership> invalidOwnerships = List.of(
                noBookIdOwnership, noLibraryIdOwnership, emptyOwnership,
                invalidBookIdOwnership, invalidLibraryIdOwnership);

        when(bookRepository.existsById(invalidBookId)).thenReturn(false);
        when(libraryRepository.existsById(invalidLibraryId)).thenReturn(false);
        when(bookRepository.existsById(validBookId)).thenReturn(true);
        when(libraryRepository.existsById(validLibraryId)).thenReturn(true);

        for (Ownership ownership : invalidOwnerships) {
            when(ownershipRepository.save(ownership)).thenReturn(ownership);
            assertThat(ownershipService.add(ownership), is(nullValue()));
            verify(ownershipRepository, times(0)).save(ownership);
        }

        when(ownershipRepository.save(validOwnership)).thenReturn(validOwnership);
        assertThat(ownershipService.add(validOwnership), is(validOwnership));
        verify(ownershipRepository, times(1)).save(validOwnership);
    }

    @Test
    void delete() {
        int validOwnershipId = new Random().nextInt(100);
        int invalidOwnershipId = new Random().nextInt(100) + 1000;
        Ownership ownership = new Ownership(validOwnershipId, 1234, 4321);

        when(ownershipRepository.findById(validOwnershipId)).thenReturn(Optional.of(ownership));
        when(ownershipRepository.findById(invalidOwnershipId)).thenReturn(Optional.empty());

        assertThat(ownershipService.delete(invalidOwnershipId), is(nullValue()));
        verify(ownershipRepository, times(0)).delete(any());

        assertThat(ownershipService.delete(validOwnershipId), is(ownership));
        verify(ownershipRepository, times(1)).delete(ownership);
    }
}
