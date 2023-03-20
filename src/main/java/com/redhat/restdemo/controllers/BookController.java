package com.redhat.restdemo.controllers;

import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/book")
public class BookController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);
    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<Iterable<Book>> getAllBooks() {
        LOGGER.info("Just hit book endpoint!");
        Iterable<Book> books = bookService.findAll();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Book>> getBookById(@PathVariable Long id) {
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

    @PostMapping("/add")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Long isbn = book.getIsbn();
        if(isbn == null) {
            LOGGER.info("Book must have ISBN!");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Book createdBook = bookService.addBook(book);
        LOGGER.info("book successfully added!");
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        if (updatedBook == null) {
            LOGGER.info("book was not found. Maybe try creating a new one?");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Library updated successfully!");
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable Long id) {
        Book deletedBook = bookService.deleteBook(id);
        if (deletedBook == null) {
            LOGGER.info("book with given ID does not exists, so it can not be deleted");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("book deleted successfully!");
        return new ResponseEntity<>(deletedBook, HttpStatus.OK);
    }
}
