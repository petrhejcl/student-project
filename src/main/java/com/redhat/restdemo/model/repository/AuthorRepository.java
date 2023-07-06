package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Small example of repository with custom query.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
    @Query(value = "SELECT a.id, a.name, a.surname, a.year_of_birth\n" +
            "FROM author a\n" +
            "JOIN authorship auth ON a.id = auth.author_id\n" +
            "JOIN book b ON b.id = auth.book_id\n" +
            "WHERE b.id = :bookid", nativeQuery = true)
    Iterable<Author> findAuthorsByBook(@Param("bookid") Integer bookid);
}
