package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Genre;

public interface GenreService {
    Iterable<Genre> findAll();

    Genre deleteGenre(String name);
}
