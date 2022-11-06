package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Small example of repository with custom query.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
    @Query(value = "SELECT * FROM author WHERE id = :id", nativeQuery = true)
    Optional<Author> findAuthorById(@Param("id") Integer authorId);

    @Query(value = "SELECT * FROM author", nativeQuery = true)
    Iterable<Author> findAll();
}
