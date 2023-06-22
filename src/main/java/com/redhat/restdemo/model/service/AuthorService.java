package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;

import java.util.Optional;

public interface AuthorService {
    Iterable<Author> findAll();
    Optional<Author> findAuthorById(Integer id);
    Author addAuthor(Author author);
    Author updateAuthor(Integer id, Author author);
    Iterable<Author> findAuthorsByBook(Integer id);
    Author deleteAuthor(Integer id);
}
