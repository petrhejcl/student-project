package com.redhat.restdemo.model.service;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import org.springframework.data.util.Pair;

public interface AuthorshipService {
    Authorship add(Authorship authorship);

    Authorship delete(Integer id);
}
