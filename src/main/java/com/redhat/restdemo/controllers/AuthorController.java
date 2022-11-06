package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    private final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

    @GetMapping("/authors")
    public ResponseEntity<Iterable<Author>> listAuthors() {
        LOGGER.info("Just hit author endpoint!");
        Iterable<Author> authors = authorService.findAll();
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @PostMapping("/author/add")
    public ResponseEntity<Author> addAuthor(@RequestBody Author author) {
        Author createdAuthor =  authorService.addAuthor(author);
        return new ResponseEntity<>(createdAuthor, HttpStatus.CREATED);
    }
}
