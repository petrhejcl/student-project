package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.entity.Ownership;

public interface OwnershipService {
    Iterable<Ownership> findAll();
    Ownership add(Ownership ownership);
    Ownership delete(Integer id);
}
