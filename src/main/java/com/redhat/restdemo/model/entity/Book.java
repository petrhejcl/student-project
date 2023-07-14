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

    @Column(name = "name")
    private String name;

    @Column(name = "year_of_release")
    private Integer yearOfRelease;

    @Column(name = "genre")
    private String genre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) && Objects.equals(isbn, book.isbn) && Objects.equals(name, book.name) && Objects.equals(yearOfRelease, book.yearOfRelease) && Objects.equals(genre, book.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isbn, name, yearOfRelease, genre);
    }

    public Book(Book book) {
        isbn = book.getIsbn();
        name = book.getName();
        yearOfRelease = book.getYearOfRelease();
        genre = book.getGenre();
    }

    public Book(Long isbn, String name, Integer yearOfRelease, String genre) {
        this.isbn = isbn;
        this.name = name;
        this.yearOfRelease = yearOfRelease;
        this.genre = genre;
    }
}
