package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Authorship;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorshipRepository extends CrudRepository<Authorship, Integer> {
    Iterable<Authorship> findAuthorshipsByAuthorId(Integer authorId);

    Iterable<Authorship> findAuthorshipsByBookId(Integer bookId);
}
