package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.service.LibraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/library")
public class LibraryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);
    @Autowired
    private LibraryService libraryService;

    @GetMapping
    public ResponseEntity<Iterable<Library>> getAllLibraries() {
        LOGGER.info("All libraries listed");
        Iterable<Library> library = libraryService.findAll();
        return new ResponseEntity<>(library, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Library>> getLibraryById(@PathVariable Integer id) {
        Optional<Library> library = libraryService.findLibraryById(id);
        if (library.isEmpty()) {
            LOGGER.info("Library with id " + id + " found");
            return new ResponseEntity<>(library, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library with id " + id + " not found");
        return new ResponseEntity<>(library, HttpStatus.OK);
    }

    //TODO: getLibrariesByBookId

    @PostMapping("/add")
    public ResponseEntity<Library> addLibrary(@RequestBody Library library) {
        Library createdLibrary = libraryService.addLibrary(library);
        LOGGER.info("Library successfully added!");
        return new ResponseEntity<>(createdLibrary, HttpStatus.CREATED);
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<Library> updateLibrary(@PathVariable Integer id, @RequestBody Library library) {
        Library updatedLibrary = libraryService.updateLibrary(id, library);
        if (updatedLibrary == null) {
            LOGGER.info("Attempt to update invalid library");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Library updated successfully!");
        return new ResponseEntity<>(updatedLibrary, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Library> deleteLibrary(@PathVariable Integer id) {
        Library deletedLibrary = libraryService.deleteLibrary(id);
        if (deletedLibrary == null) {
            LOGGER.info("Attempt to delete invalid library");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Library deleted successfully!");
        return new ResponseEntity<>(deletedLibrary, HttpStatus.ACCEPTED);
    }
}
