package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Ownership;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnershipRepository extends CrudRepository<Ownership, Integer> {
}
