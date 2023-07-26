package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Book existingBook = findBookById(id).orElseThrow();

        if (book.getId() != null) {
            throw new IllegalArgumentException("Attempt to change id of book");
        }

        Long newIsbn = book.getIsbn();
        if (newIsbn != null) {
            existingBook.setIsbn(newIsbn);
        }
        String newName = book.getName();
        if (newName != null) {
            existingBook.setName(newName);
        }
        Integer newYearOfRelease = book.getYearOfRelease();
        if (newYearOfRelease != null) {
            existingBook.setYearOfRelease(newYearOfRelease);
        }
        String newGenre = book.getGenre();
        if (newGenre != null) {
            existingBook.setGenre(newGenre);
        }
        return addBook(existingBook);
    }
    @Override
    public Book deleteBook(Integer id) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isEmpty()) {
            throw new NoSuchElementException("Book not found");
        }
        authorshipRepository.deleteAll(authorshipRepository.findAuthorshipsByBookId(id));
        ownershipRepository.deleteAll(ownershipRepository.findOwnershipsByBookId(id));
        bookRepository.deleteById(id);
        return book.get();
    }
}
