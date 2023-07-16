package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.service.BookService;
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
@RequestMapping("/book")
public class BookController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);
    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<Iterable<Book>> getAllBooks() {
        LOGGER.info("Just hit books endpoint!");
        Iterable<Book> books = bookService.findAll();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Book>> getBookById(@PathVariable Integer id) {
        Optional<Book> book = bookService.findBookById(id);
        if (book.isEmpty()) {
            LOGGER.info("Book with given ID does not exists");
            return new ResponseEntity<>(book, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Book found!");
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<Iterable<Book>> getBooksByGenre(@PathVariable String genre) {
        Iterable<Book> books = bookService.findBooksByGenre(genre);
        LOGGER.info("Books found!");
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/author/{id}")
    public ResponseEntity<Iterable<Book>> getBooksByAuthor(@PathVariable Integer id) {
        Iterable<Book> books = bookService.findBooksByAuthor(id);
        LOGGER.info("Books found!");
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/library/{id}")
    public ResponseEntity<Iterable<Book>> getBooksByLibrary(@PathVariable Integer id) {
        Iterable<Book> books = bookService.findBooksByLibrary(id);
        LOGGER.info("Books found!");
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Long isbn = book.getIsbn();
        if(isbn == null) {
            LOGGER.info("Book does not have ISBN!");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Book createdBook = bookService.addBook(book);
        LOGGER.info("Book successfully added!");
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            LOGGER.info("Book updated successfully!");
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable Integer id) {
        try {
            Book deletedBook = bookService.deleteBook(id);
            LOGGER.info("Book deleted successfully!");
            return new ResponseEntity<>(deletedBook, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
