package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Small example of repository with custom query.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
}
