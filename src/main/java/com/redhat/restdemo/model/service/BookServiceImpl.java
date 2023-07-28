package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Service
public class BookServiceImpl implements BookService{

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorshipRepository authorshipRepository;

    @Autowired
    private OwnershipRepository ownershipRepository;

    @Override
    public Iterable<Book> findAll() {
        return bookRepository.findAll();
    }
    @Override
    public Optional<Book> findBookById(Integer id) {
        return bookRepository.findById(id);
    }

    @Override
    public Iterable<Book> findBooksByGenre(String genre) {
        return bookRepository.findBooksByGenre(genre);
    }

    @Override
    public Iterable<Book> findBooksByAuthor (Integer id) {
        return bookRepository.findBooksByAuthor(id);
    }

    @Override
    public Iterable<Book> findBooksByLibrary(Integer id) {
        return bookRepository.findBooksByLibrary(id);
    }

    @Override
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }
    @Override
    public Book updateBook(Integer id, Book book) {
        Optional<Book> existingBook = findBookById(id);
        if (existingBook.isEmpty()) {
            return null;
        }
        Book updatedBook = existingBook.get();
        Long newIsbn = book.getIsbn();
        if (newIsbn != null) {
            updatedBook.setIsbn(newIsbn);
        }
        String newName = book.getName();
        if (newName != null) {
            updatedBook.setName(newName);
        }
        Integer newYearOfRelease = book.getYearOfRelease();
        if (newYearOfRelease != null) {
            updatedBook.setYearOfRelease(newYearOfRelease);
        }
        String newGenre = book.getGenre();
        if (newGenre != null) {
            updatedBook.setGenre(newGenre);
        }
        return addBook(updatedBook);
    }
    @Override
    public Book deleteBook(Integer id) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isEmpty()) {
            return null;
        }
        authorshipRepository.deleteAll(authorshipRepository.findAuthorshipsByBookId(id));
        ownershipRepository.deleteAll(ownershipRepository.findOwnershipsByBookId(id));
        bookRepository.deleteById(id);
        return book.get();
    }
}
