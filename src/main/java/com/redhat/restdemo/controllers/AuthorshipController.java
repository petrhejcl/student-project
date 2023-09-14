package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.service.AuthorshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authorship")
public class AuthorshipController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorshipService authorshipService;

    @GetMapping
    public ResponseEntity<Iterable<Authorship>> getAllAuthorship() {
        LOGGER.info("Authorships listed");
        Iterable<Authorship> authors = authorshipService.findAll();
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Authorship> addAuthorship(@RequestBody Authorship authorship) {
        Authorship createdAuthorship = authorshipService.add(authorship);
        if (createdAuthorship == null) {
            LOGGER.info("Attempt to create authorship with invalid or empty book or author id");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Authorship successfully added!");
        return new ResponseEntity<>(createdAuthorship, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Authorship> deleteAuthorship(@PathVariable Integer id) {
        Authorship deletedAuthorship = authorshipService.delete(id);
        if (deletedAuthorship == null) {
            LOGGER.info("Attempt to delete invalid authorship");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Authorship successfully deleted!");
        return new ResponseEntity<>(deletedAuthorship, HttpStatus.OK);
    }
}
