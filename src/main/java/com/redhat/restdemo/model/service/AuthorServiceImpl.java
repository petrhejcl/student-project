package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository repository;

    @Override
    public Optional<Author> findAuthorById(int id) {
        return repository.findAuthorById(id);
    }

    @Override
    public Iterable<Author> findAll() {
        return repository.findAll();
    }

    @Override
    public Author addAuthor(Author author) {
        if (author.getId() == 0) {
            Author x = new Author(author);
            return repository.save(x);
        } else {
            return repository.save(author);
        }
    }
}
