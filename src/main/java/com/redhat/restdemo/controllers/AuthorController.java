package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.service.AuthorService;
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
@RequestMapping("/author")
public class AuthorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public ResponseEntity<Iterable<Author>> getAllAuthors() {
        LOGGER.info("Just hit author endpoint!");
        Iterable<Author> authors = authorService.findAll();
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Author>> getAuthorById(@PathVariable Integer id) {
        Optional<Author> author = authorService.findAuthorById(id);
        if (author.isEmpty()) {
            LOGGER.info("Author not found");
            return new ResponseEntity<>(author, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Author found!");
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<Iterable<Author>> getAuthorsByBook(@PathVariable Integer id) {
        Iterable<Author> authors = authorService.findAuthorsByBook(id);
        LOGGER.info("Authors found!");
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Author> addAuthor(@RequestBody Author author) {
        Author createdAuthor = authorService.addAuthor(author);
        LOGGER.info("Author successfully added!");
        return new ResponseEntity<>(createdAuthor, HttpStatus.CREATED);
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Integer id, @RequestBody Author author) {
        try {
            Author updatedAuthor = authorService.updateAuthor(id, author);
            LOGGER.info("Author updated successfully!");
            return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Author> deleteAuthor(@PathVariable Integer id) {
        try {
            Author deletedAuthor = authorService.deleteAuthor(id);
            LOGGER.info("Author deleted successfully!");
            return new ResponseEntity<>(deletedAuthor, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
