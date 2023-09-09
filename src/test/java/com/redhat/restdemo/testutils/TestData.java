package com.redhat.restdemo.testutils;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestData {
    public static List<Author> authors = new LinkedList<>();

    public static List<Book> books = new LinkedList<>();

    public static List<Library> libraries = new LinkedList<>();

    public static Map<Author, Book> authorship = new HashMap<>();

    public static Map<Book, Library> ownership = new HashMap<>();

    static {
        authors.add(new Author("J.K.", "Rowling", 1965));
        authors.add(new Author("George", "Orwell", 1903));
        authors.add(new Author("Jane", "Austen", 1775));
        authors.add(new Author("Ernest", "Hemingway", 1899));
        authors.add(new Author("Maya", "Angelou", 1928));
        authors.add(new Author("Charles", "Bukowski", 1920));

        authors = Collections.unmodifiableList(authors);

        books.add(new Book(9780747532743L, "Harry Potter and the Sorcerer's Stone", 1997, "Fantasy"));
        books.add(new Book(9780451524935L, "Nineteen Eighty-Four", 1949, "Dystopian Fiction"));
        books.add(new Book(9780141439518L, "Pride and Prejudice", 1813, "Classic Fiction"));
        books.add(new Book(9780684801223L, "The Old Man and the Sea", 1952, "Fiction"));
        books.add(new Book(9780345514400L, "I Know Why the Caged Bird Sings", 1969, "Autobiography"));
        books.add(new Book(9780876857632L, "Post Office", 1971, "Fiction"));

        books = Collections.unmodifiableList(books);

        libraries.add(new Library("Central Library", "New York", "Main Street", 123, "The largest library in the city"));
        libraries.add(new Library("Community Library", "Chicago", "Elm Street", 456, "A community-focused library with diverse collections"));
        libraries.add(new Library("Tech Library", "San Francisco", "Oak Street", 789, "Specializes in technology and computer science resources"));
        libraries.add(new Library("Historical Library", "London", "Abbey Road", 10, "Preserves historical manuscripts and rare books"));
        libraries.add(new Library("Children's Library", "Sydney", "Park Street", 321, "Offers a wide range of books and activities for children"));
        libraries.add(new Library("University Library", "Tokyo", "University Avenue", 987, "Supports academic research and provides resources for students"));

        libraries = Collections.unmodifiableList(libraries);

        authorship.put(authors.get(0), books.get(0));
        authorship.put(authors.get(1), books.get(1));
        authorship.put(authors.get(2), books.get(2));
        authorship.put(authors.get(3), books.get(3));
        authorship.put(authors.get(4), books.get(4));
        authorship.put(authors.get(5), books.get(5));

        authorship = Collections.unmodifiableMap(authorship);

        ownership.put(books.get(0), libraries.get(0));
        ownership.put(books.get(1), libraries.get(1));
        ownership.put(books.get(2), libraries.get(2));
        ownership.put(books.get(3), libraries.get(3));
        ownership.put(books.get(4), libraries.get(4));
        ownership.put(books.get(5), libraries.get(5));

        authorship = Collections.unmodifiableMap(authorship);
    }
}
