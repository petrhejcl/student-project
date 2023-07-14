package com.redhat.restdemo.controllers;


import com.redhat.restdemo.model.entity.Genre;
import com.redhat.restdemo.model.service.GenreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/genre")
public class GenreController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenreController.class);
    @Autowired
    private GenreService genreService;

    @GetMapping
    public ResponseEntity<Iterable<Genre>> getAllGenres() {
        LOGGER.info("Just hit genre endpoint!");
        Iterable<Genre> genres = genreService.findAll();
        return new ResponseEntity<>(genres, HttpStatus.OK);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Genre> deleteGenre(@PathVariable String name) {
        Genre deletedGenre = genreService.deleteGenre(name);
        if (deletedGenre == null) {
            LOGGER.info("Given genre does not exists, so it can not be deleted");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Genre deleted successfully!");
        return new ResponseEntity<>(deletedGenre, HttpStatus.OK);
    }
}
