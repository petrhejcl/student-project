package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Iterable<Book> findAll();
    Optional<Book> findBookById(Integer id);
    Iterable<Book> findBooksByGenre(String genre);
    Iterable<Book> findBooksByAuthor (Integer id);
    Book addBook(Book book);
    Book updateBook(Integer id, Book book);
    Book deleteBook(Integer id);
}
