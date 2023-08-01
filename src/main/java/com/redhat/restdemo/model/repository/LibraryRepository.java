package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Library;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends CrudRepository<Library, Integer> {
    @Query(value = "SELECT l.id, l.name, l.city, l.street, l.street_number, l.description " +
            "FROM library l " +
            "INNER JOIN ownership o ON l.id = o.library_id " +
            "WHERE o.book_id = :bookId", nativeQuery = true)
    Iterable<Library> findLibrariesByBook(@Param("bookId") Integer bookId);
}
