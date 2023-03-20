package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;

import java.util.Optional;

public interface AuthorService {
    Iterable<Author> findAll();
    Optional<Author> findAuthorById(Integer id);
    Author addAuthor(Author author);
    Author updateAuthor(Integer id, Author author);
    Author deleteAuthor(Integer id);
    Iterable<Book> findBooksByAuthor (Integer id);
    Book addBookToAuthor(Integer authorId, Book book);
    Book deleteBookFromAuthor(Integer authorId, Long isbn);
}
