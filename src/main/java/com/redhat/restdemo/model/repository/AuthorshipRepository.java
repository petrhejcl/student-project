package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Authorship;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorshipRepository extends CrudRepository<Authorship, Integer> {
    @Query(value = "SELECT id, book_id, author_id FROM authorship WHERE author_id = :authorId AND book_id = :bookId", nativeQuery = true)
    Authorship findAuthorship(@Param("authorId") Integer authorId, @Param("bookId") Integer bookId);

    @Query(value = "SELECT id, book_id, author_id FROM authorship WHERE author_id = :authorId", nativeQuery = true)
    Iterable<Authorship> findAuthorshipFromAuthor(@Param("authorId") Integer authorId);

    @Query(value = "SELECT id, book_id, author_id FROM authorship WHERE id = :bookId", nativeQuery = true)
    Iterable<Authorship> findAuthorshipFromBook(@Param("bookId") Integer bookId);
}
