package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.service.LibraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/library")
public class LibraryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);
    @Autowired
    private LibraryService libraryService;

    @GetMapping
    public ResponseEntity<Iterable<Library>> getAllLibraries() {
        LOGGER.info("Just hit library endpoint!");
        Iterable<Library> library = libraryService.findAll();
        return new ResponseEntity<>(library, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Library>> getLibraryById(@PathVariable Integer id) {
        Optional<Library> library = libraryService.findLibraryById(id);
        if (library.isEmpty()) {
            LOGGER.info("Library with given ID does not exists");
            return new ResponseEntity<>(library, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library found!");
        return new ResponseEntity<>(library, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Library> addLibrary(@RequestBody Library library) {
        Library createdLibrary = libraryService.addLibrary(library);
        LOGGER.info("Library successfully added!");
        return new ResponseEntity<>(createdLibrary, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Library> updateLibrary(@PathVariable Integer id, @RequestBody Library library) {
        Library updatedLibrary = libraryService.updateLibrary(id, library);
        if (updatedLibrary == null) {
            LOGGER.info("Library was not found. Maybe try creating a new one?");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library updated successfully!");
        return new ResponseEntity<>(updatedLibrary, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Library> deleteLibrary(@PathVariable Integer id) {
        Library deletedLibrary = libraryService.deleteLibrary(id);
        if (deletedLibrary == null) {
            LOGGER.info("Library with given ID does not exists, so it can not be deleted");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library deleted successfully!");
        return new ResponseEntity<>(deletedLibrary, HttpStatus.OK);
    }
}
