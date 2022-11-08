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

/**
 * This is a short example of mapping class that should be completed by all other attributes.
 */
@Entity
@Table(name = "author")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Author {
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    int id;

    private String name;
    private String surname;

    public Author(Author author) {
        name = author.getName();
        surname = author.getSurname();
    }

    public Author(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }
}
