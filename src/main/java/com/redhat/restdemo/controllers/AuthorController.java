package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            LOGGER.info("Author with given ID does not exists");
            return new ResponseEntity<>(author, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Author found!");
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<Iterable<Book>> getBooksByAuthor(@PathVariable Integer id) {
        Iterable<Book> author = authorService.findBooksByAuthor(id);
        LOGGER.info("Books found!");
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Author> addAuthor(@RequestBody Author author) {
        Author createdAuthor = authorService.addAuthor(author);
        LOGGER.info("Author successfully added!");
        return new ResponseEntity<>(createdAuthor, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/books")
    public ResponseEntity<Book> addBookToAuthor(@RequestBody Book book, @PathVariable Integer id) {
        Book addedBook = authorService. addBookToAuthor(id, book);
        if (addedBook == null) {
            LOGGER.info("Please, specify book's ISBN");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Book was successfully added!");
        return new ResponseEntity<>(addedBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Integer id, @RequestBody Author author) {
        Author updatedAuthor = authorService.updateAuthor(id, author);
        if (updatedAuthor == null) {
            LOGGER.info("Author was not found. Maybe try creating a new one?");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library updated successfully!");
        return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Author> deleteAuthor(@PathVariable Integer id) {
        Author deletedAuthor = authorService.deleteAuthor(id);
        if (deletedAuthor == null) {
            LOGGER.info("Author with given ID does not exists, so it can not be deleted");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Author deleted successfully!");
        return new ResponseEntity<>(deletedAuthor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/books/delete")
    public ResponseEntity<Book> deleteBookFromAuthor(@PathVariable Integer id, @RequestBody Long isbn) {
        Book deletedBook = authorService.deleteBookFromAuthor(id, isbn);
        if (deletedBook == null) {
            LOGGER.info("Book with given ISBN either is not listed or is not written by author with given id");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Authorship of book by author was successfully deleted!");
        return new ResponseEntity<>(deletedBook, HttpStatus.OK);
    }
}
