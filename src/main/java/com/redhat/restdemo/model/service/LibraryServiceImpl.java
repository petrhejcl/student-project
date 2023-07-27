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
        try {
            if (library.getId() != null) {
                throw new IllegalArgumentException();
            }
            Library existingLibrary = findLibraryById(id).orElseThrow();
            String newName = library.getName();
            if (newName != null) {
                existingLibrary.setName(newName);
            }
            String newCity = library.getCity();
            if (newCity != null) {
                existingLibrary.setCity(newCity);
            }
            String newStreet = library.getStreet();
            if (newStreet != null) {
                existingLibrary.setStreet(newStreet);
            }
            Integer newStreetNumber = library.getStreetNumber();
            if (newStreetNumber != null) {
                existingLibrary.setStreetNumber(newStreetNumber);
            }
            String newDescription = library.getDescription();
            if (newDescription != null) {
                existingLibrary.setDescription(newDescription);
            }
            return addLibrary(existingLibrary);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Library deleteLibrary(Integer id) {
        try {
            Library library = findLibraryById(id).get();
            libraryRepository.deleteById(id);
            ownershipRepository.deleteAll(ownershipRepository.findOwnershipsByLibraryId(id));
            return library;
        } catch (Exception e) {
            return null;
        }
    }
}
