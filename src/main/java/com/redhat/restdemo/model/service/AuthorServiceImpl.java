package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

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
        try {
            Author existingAuthor = findAuthorById(id).orElseThrow();
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
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Author deleteAuthor(Integer id) {
        try {
            Author author = findAuthorById(id).get();
            authorRepository.deleteById(id);
            return author;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Iterable<Book> findBooksByAuthor (Integer id) {
        return bookRepository.findBooksByAuthor(id);
    }

    @Override
    public Book addBookToAuthor(Integer authorId, Book book) {
        Long isbn = book.getIsbn();
        if (isbn == null) {
            return null;
        }
        Optional<Book> existingBook = bookRepository.findById(isbn);
        if (existingBook.isEmpty()) {
            bookRepository.save(book);
        }
        else {
            book = existingBook.get();
        }
        Authorship authorship = new Authorship(isbn, authorId);
        authorshipRepository.save(authorship);
        return book;
    }

    @Override
    public Book deleteBookFromAuthor(Integer authorId, Long isbn) {
        Integer authorshipId = authorshipRepository.findAuthorshipId(authorId, isbn);
        if (authorshipId == null) {
            return null;
        }
        authorshipRepository.deleteById(authorshipId);
        return bookRepository.findById(isbn).get();
    }
}
