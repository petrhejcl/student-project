package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorshipRepository extends CrudRepository<Authorship, Integer> {
    @Query(value = "SELECT id FROM authorship WHERE author_id = :authorId AND isbn = :isbn", nativeQuery = true)
    Integer findAuthorshipId(@Param("authorId") Integer authorId, @Param("isbn") Long isbn);
}
