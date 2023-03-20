package com.redhat.restdemo.model.repository;

import com.redhat.restdemo.model.entity.Library;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends CrudRepository<Library, Integer> {
}
