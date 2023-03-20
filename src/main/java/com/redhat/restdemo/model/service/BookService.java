package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Iterable<Book> findAll();
    Optional<Book> findBookById(Long id);
    Iterable<Book> findBooksByGenre(String genre);
    Book addBook(Book book);
    Book updateBook(Long id, Book book);
    Book deleteBook(Long id);
}
