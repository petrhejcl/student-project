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
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    String ISBN;

    private String name;
    private Date release;
    private String genre;

    public Book(Book book) {
        name = book.getName();
        release = book.getRelease();
        genre = book.getGenre();
    }

    public Book(String name, Date release, String genre) {
        this.name = name;
        this.release = release;
        this.genre = genre;
    }
}
