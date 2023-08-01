package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public Iterable<Authorship> findAll() {
        return authorshipRepository.findAll();
    }

    @Override
    public Authorship add(Authorship authorship) {
        if (authorship.getBookId() == null || authorship.getAuthorId() == null ||
                !authorRepository.existsById(authorship.getAuthorId()) ||
                !bookRepository.existsById(authorship.getBookId())) {
            return null;
        }
        return authorshipRepository.save(authorship);
    }

    @Override
    public Authorship delete(Integer id) {
        Optional<Authorship> authorship = authorshipRepository.findById(id);
        if (authorship.isEmpty()) {
            return null;
        }
        authorshipRepository.delete(authorship.get());
        return authorship.get();
    }
}
