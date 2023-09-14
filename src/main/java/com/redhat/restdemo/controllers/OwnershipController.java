package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.service.OwnershipService;
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
@RequestMapping("/ownership")
public class OwnershipController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping
    public ResponseEntity<Iterable<Ownership>> getAllOwnerships() {
        LOGGER.info("Ownerships listed");
        Iterable<Ownership> ownerships = ownershipService.findAll();
        return new ResponseEntity<>(ownerships, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Ownership> addOwnership(@RequestBody Ownership ownership) {
        Ownership createdOwnership = ownershipService.add(ownership);
        if (createdOwnership == null) {
            LOGGER.info("Attempt to create Ownership with invalid or empty book or library id");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Ownership successfully added!");
        return new ResponseEntity<>(createdOwnership, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Ownership> deleteOwnership(@PathVariable Integer id) {
        Ownership deletedOwnership = ownershipService.delete(id);
        if (deletedOwnership == null) {
            LOGGER.info("Attempt to delete invalid ownership");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Ownership deleted successfully");
        return new ResponseEntity<>(deletedOwnership, HttpStatus.OK);
    }
}
