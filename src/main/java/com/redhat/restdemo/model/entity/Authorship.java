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

@Entity
@Table(name = "authorship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Authorship{
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    Integer id;

    @Column(name = "isbn")
    private Long isbn;

    @Column(name = "author_id")
    private Integer authorId;

    public Authorship(Long isbn, Integer authorId) {
        this.isbn = isbn;
        this.authorId = authorId;
    }

    public Authorship(Authorship authorship) {
        isbn = authorship.getIsbn();
        authorId = authorship.getAuthorId();
    }
}
