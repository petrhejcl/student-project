package com.redhat.restdemo.utils;

import com.redhat.restdemo.model.entity.Author;

public class utils {
    public static Long countAuthors(Iterable<Author> authors) {
        Long authorCounter = 0L;
        for (Author author: authors) {
            authorCounter++;
        }
        return authorCounter;
    }
}
