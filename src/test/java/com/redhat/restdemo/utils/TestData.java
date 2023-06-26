package com.redhat.restdemo.utils;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;

import java.util.HashMap;
import java.util.LinkedList;

public class TestData {
    public static LinkedList<Author> authors = new LinkedList<>();

    public static LinkedList<Book> books = new LinkedList<>();

    public static HashMap<Author, Book> authorship = new HashMap<>();

    static {
        authors.add(new Author("J.K.", "Rowling", 1965));
        authors.add(new Author("George", "Orwell", 1903));
        authors.add(new Author("Jane", "Austen", 1775));
        authors.add(new Author("Ernest", "Hemingway", 1899));
        authors.add(new Author("Maya", "Angelou", 1928));
        authors.add(new Author("Charles", "Bukowski", 1920));

        books.add(new Book(9780747532743L, "Harry Potter and the Sorcerer's Stone", 1997, "Fantasy"));
        books.add(new Book(9780451524935L, "Nineteen Eighty-Four", 1949, "Dystopian Fiction"));
        books.add(new Book(9780141439518L, "Pride and Prejudice", 1813, "Classic Fiction"));
        books.add(new Book(9780684801223L, "The Old Man and the Sea", 1952, "Fiction"));
        books.add(new Book(9780345514400L, "I Know Why the Caged Bird Sings", 1969, "Autobiography"));
        books.add(new Book(9780876857632L, "Post Office", 1971, "Fiction"));

        authorship.put(authors.get(0), books.get(0));
        authorship.put(authors.get(1), books.get(1));
        authorship.put(authors.get(2), books.get(2));
        authorship.put(authors.get(3), books.get(3));
        authorship.put(authors.get(4), books.get(4));
        authorship.put(authors.get(5), books.get(5));
    }
}
