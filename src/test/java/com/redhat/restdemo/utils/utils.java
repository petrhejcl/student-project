package com.redhat.restdemo.utils;

import com.redhat.restdemo.model.entity.Author;

import java.util.Objects;

public class utils {
    public static Long countGetResult(Iterable<Object> objects) {
        Long counter = 0L;
        for (Object object: objects) {
            counter++;
        }
        return counter;
    }
}
