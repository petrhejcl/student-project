package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorshipRepository authorshipRepository;

    @Override
    public Iterable<Author> findAll() {
        return authorRepository.findAll();
    }

    @Override
    public Optional<Author> findAuthorById(Integer id) {
        return authorRepository.findById(id);
    }

    @Override
    public Iterable<Author> findAuthorsByBook(Integer id) {
        return authorRepository.findAuthorsByBook(id);
    }

    @Override
    public Author addAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public Author updateAuthor(Integer id, Author author) {
        Optional<Author> existingAuthor = findAuthorById(id);
        if (existingAuthor.isEmpty()) {
            return null;
        }
        Author updatedAuthor= existingAuthor.get();
        String newName = author.getName();
        if (newName != null) {
            updatedAuthor.setName(newName);
        }
        String newSurname = author.getSurname();
        if (newSurname != null) {
            updatedAuthor.setSurname(newSurname);
        }
        Integer newYearOfBirth = author.getYearOfBirth();
        if (newYearOfBirth != null) {
            updatedAuthor.setYearOfBirth(newYearOfBirth);
        }
        return addAuthor(updatedAuthor);
    }

    @Override
    public Author deleteAuthor(Integer id) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isEmpty()) {
            return null;
        }
        authorshipRepository.deleteAll(authorshipRepository.findAuthorshipsByAuthorId(id));
        authorRepository.deleteById(id);
        return author.get();
    }

}
