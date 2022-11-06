package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;

import java.util.Optional;

public interface AuthorService {
    Optional<Author> findAuthorById(int integer);
    Iterable<Author> findAll();

    Author addAuthor(Author author);
}
