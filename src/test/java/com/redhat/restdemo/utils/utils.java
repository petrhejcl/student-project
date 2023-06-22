package com.redhat.restdemo.utils;

import com.redhat.restdemo.model.entity.Author;

import java.util.Objects;

public class utils {
    public static <T> Long countGetResult(Iterable<T> objects) {
        Long counter = 0L;
        for (Object object: objects) {
            counter++;
        }
        return counter;
    }
}
