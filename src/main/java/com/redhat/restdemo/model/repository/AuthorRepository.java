package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Small example of repository with custom query.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
    @Query(value = "SELECT id, name, surname, year_of_birth FROM author NATURAL INNER JOIN book WHERE id = :bookid", nativeQuery = true)
    Iterable<Author> findAuthorsByBook(@Param("bookid") Integer bookid);
}
