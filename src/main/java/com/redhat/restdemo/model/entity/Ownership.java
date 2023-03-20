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
@Table(name = "ownership")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ownership{
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    Integer id;

    @Column(name = "libraryId")
    private Integer libraryId;

    @Column(name = "isbn")
    private Long isbn;

    public Ownership(Ownership ownership) {
        libraryId = ownership.getLibraryId();
        isbn = ownership.getIsbn();
    }

    public Ownership(Integer libraryId, Long isbn) {
        this.libraryId = libraryId;
        this.isbn = isbn;
    }

}
