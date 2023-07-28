package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Library;

import java.util.Optional;


public interface LibraryService {
    Iterable<Library> findAll();
    Optional<Library> findLibraryById(Integer id);
    Iterable<Library> findLibrariesByBookId(Integer bookId);
    Library addLibrary(Library library);
    Library updateLibrary(Integer id, Library library);
    Library deleteLibrary(Integer id);
}
