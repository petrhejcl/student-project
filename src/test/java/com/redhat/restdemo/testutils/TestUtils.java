package com.redhat.restdemo.testutils;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;

public class TestUtils {
    public static <T> Long countIterable(Iterable<T> objects) {
        Long counter = 0L;
        for (Object object: objects) {
            counter++;
        }
        return counter;
    }

    public static void resetTestDataIDs() {
        for (Book book : TestData.books) {
            book.setId(null);
        }
        for (Author author : TestData.authors) {
            author.setId(null);
        }
        for (Library library : TestData.libraries) {
            library.setId(null);
        }
    }
}
