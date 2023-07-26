package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Ownership;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnershipRepository extends CrudRepository<Ownership, Integer> {
    Iterable<Ownership> findOwnershipsByBookId(Integer bookId);

    Iterable<Ownership> findOwnershipsByLibraryId(Integer libraryId);
}
