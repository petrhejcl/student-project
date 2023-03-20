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

import java.sql.Date;


@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
public class Book {
    private @Id
    @Column(name = "isbn")
    Long isbn;

    @Column(name = "name")
    private String name;

    @Column(name = "year_of_release")
    private Integer yearOfRelease;

    @Column(name = "genre")
    private String genre;

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
