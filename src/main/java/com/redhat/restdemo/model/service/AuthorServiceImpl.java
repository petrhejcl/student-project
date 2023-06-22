package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public Author addAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public Author updateAuthor(Integer id, Author author) {
        Author existingAuthor = findAuthorById(id).orElseThrow();

        if (author.getId() != null) {
            throw new IllegalArgumentException("You can not change id of author");
        }

        String newName = author.getName();
        if (newName != null) {
            existingAuthor.setName(newName);
        }
        String newSurname = author.getSurname();
        if (newSurname != null) {
            existingAuthor.setSurname(newSurname);
        }
        Integer newYearOfBirth = author.getYearOfBirth();
        if (newYearOfBirth != null) {
            existingAuthor.setYearOfBirth(newYearOfBirth);
        }
        return addAuthor(existingAuthor);
    }

    @Override
    public Author deleteAuthor(Integer id) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isEmpty()) {
            throw new NoSuchElementException("Author with given id does not exist, so it can not be deleted");
        }
        authorshipRepository.deleteAll(authorshipRepository.findAuthorshipFromAuthor(id));
        authorRepository.deleteById(id);
        return author.get();
    }

    @Override
    public Iterable<Author> findAuthorsByBook(Integer id) {
        return authorRepository.findAuthorsByBook(id);
    }

}
