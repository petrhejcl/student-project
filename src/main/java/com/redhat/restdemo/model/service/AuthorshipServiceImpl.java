package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Service
public class AuthorshipServiceImpl implements AuthorshipService {

    @Autowired
    AuthorshipRepository authorshipRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Override
    public Authorship add(Authorship authorship) {
        if (authorRepository.findById(authorship.getAuthorId()).isEmpty() ||
                bookRepository.findById(authorship.getId()).isEmpty()) {
            throw new NoSuchElementException("Author or book does not exit yet");
        }
        authorshipRepository.save(authorship);
        return authorship;
    }

    @Override
    public Authorship delete(Integer id) {
        Optional<Authorship> authorship = authorshipRepository.findById(id);
        if (authorship.isEmpty()) {
            throw new NoSuchElementException("Authorship with given id does not exist yet");
        }
        authorshipRepository.delete(authorship.get());
        return authorship.get();
    }
}
