package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Genre;
import com.redhat.restdemo.model.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Transactional
@Service
public class GenreServiceImpl implements GenreService{

    @Autowired
    private GenreRepository genreRepository;

    public Iterable<Genre> findAll() {
        return genreRepository.findAll();
    }

    public Genre deleteGenre(String name) {
        try {
            Genre genre = genreRepository.findById(name).get();
            genreRepository.deleteById(name);
            return genre;
        } catch (Exception e) {
            return null;
        }
    }

}
