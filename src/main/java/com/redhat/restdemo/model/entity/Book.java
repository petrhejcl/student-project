package com.redhat.restdemo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;


@Entity
@Table(name = "book")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    Integer id;

    @Column(name = "isbn")
    Long isbn;

    @Column(name = "title")
    private String title;

    @Column(name = "year_of_release")
    private Integer yearOfRelease;

    @Column(name = "genre")
    private String genre;

    /*
     * Two objects are considered equal if all their attributes (except ids) are the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn) && Objects.equals(title, book.title) && Objects.equals(yearOfRelease, book.yearOfRelease) && Objects.equals(genre, book.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, title, yearOfRelease, genre);
    }

    public Book(Book book) {
        isbn = book.getIsbn();
        title = book.getTitle();
        yearOfRelease = book.getYearOfRelease();
        genre = book.getGenre();
    }

    public Book(Long isbn, String title, Integer yearOfRelease, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.yearOfRelease = yearOfRelease;
        this.genre = genre;
    }
}
