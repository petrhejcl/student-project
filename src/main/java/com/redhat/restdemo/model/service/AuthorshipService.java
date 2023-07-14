package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Authorship;

public interface AuthorshipService {
    Iterable<Authorship> findAll();
    Authorship add(Authorship authorship);
    Authorship delete(Integer id);
}
