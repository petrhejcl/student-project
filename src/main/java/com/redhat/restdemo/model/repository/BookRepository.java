package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BookRepository extends CrudRepository<Book, Integer> {
    @Query(value = "SELECT * FROM book WHERE genre = :genre", nativeQuery = true)
    Iterable<Book> findBooksByGenre(@Param("genre") String genreName);

    @Query(value = "SELECT id, genre, name, year_of_release FROM book NATURAL INNER JOIN authorship WHERE author_id = :authorId", nativeQuery = true)
    Iterable<Book> findBooksByAuthor(@Param("authorId") Integer authorId);
}
