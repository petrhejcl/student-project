package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Library;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends CrudRepository<Library, Integer> {
    @Query(value = "SELECT l.name, l.city, l.street, l.street_number, l.description\n" +
            "FROM library l\n" +
            "JOIN ownership own ON l.id = own.library_id\n" +
            "JOIN book b ON b.id = own.book_id\n" +
            "WHERE b.id = :bookId", nativeQuery = true)
    Iterable<Library> findLibrariesByBook(@Param("bookId") Integer bookId);
}
