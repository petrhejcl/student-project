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

    @Column(name = "library_id")
    private Integer libraryId;

    @Column(name = "book_id")
    private Integer bookId;

    public Ownership(Ownership ownership) {
        libraryId = ownership.getLibraryId();
        bookId = ownership.getBookId();
    }

    public Ownership(Integer libraryId, Integer bookId) {
        this.libraryId = libraryId;
        this.bookId = bookId;
    }

}
