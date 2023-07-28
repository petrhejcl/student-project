package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Service
public class LibraryServiceImpl implements LibraryService{
    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private OwnershipRepository ownershipRepository;

    @Override
    public Iterable<Library> findAll() {
        return libraryRepository.findAll();
    }

    @Override
    public Optional<Library> findLibraryById(Integer id) {
        return libraryRepository.findById(id);
    }

    //TODO: findLibrariesByBook

    @Override
    public Library addLibrary(Library library) {
        return libraryRepository.save(library);
    }

    @Override
    public Library updateLibrary(Integer id, Library library) {
        Optional<Library> existingLibrary = findLibraryById(id);
        if (existingLibrary.isEmpty()) {
            return null;
        }
        Library updatedLibrary = existingLibrary.get();
        String newName = library.getName();
        if (newName != null) {
            updatedLibrary.setName(newName);
        }
        String newCity = library.getCity();
        if (newCity != null) {
            updatedLibrary.setCity(newCity);
        }
        String newStreet = library.getStreet();
        if (newStreet != null) {
            updatedLibrary.setStreet(newStreet);
        }
        Integer newStreetNumber = library.getStreetNumber();
        if (newStreetNumber != null) {
            updatedLibrary.setStreetNumber(newStreetNumber);
        }
        String newDescription = library.getDescription();
        if (newDescription != null) {
            updatedLibrary.setDescription(newDescription);
        }
        return addLibrary(updatedLibrary);
        
    }

    @Override
    public Library deleteLibrary(Integer id) {
        Optional<Library> library = libraryRepository.findById(id);
        if (library.isEmpty()) {
            return null;
        }
        ownershipRepository.deleteAll(ownershipRepository.findOwnershipsByLibraryId(id));
        libraryRepository.deleteById(id);
        return library.get();
    }
}
