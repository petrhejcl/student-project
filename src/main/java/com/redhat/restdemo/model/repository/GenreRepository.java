package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Genre;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends CrudRepository<Genre, String> {
}
