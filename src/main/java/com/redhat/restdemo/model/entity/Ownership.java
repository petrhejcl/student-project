package com.redhat.restdemo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ownership ownership = (Ownership) o;
        return Objects.equals(libraryId, ownership.libraryId) && Objects.equals(bookId, ownership.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryId, bookId);
    }

    public Ownership(Integer libraryId, Integer bookId) {
        this.libraryId = libraryId;
        this.bookId = bookId;
    }

}
