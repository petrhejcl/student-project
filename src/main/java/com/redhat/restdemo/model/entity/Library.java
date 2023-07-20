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
@Table(name = "library")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Library {
    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "street_number")
    private Integer streetNumber;

    @Column(name = "description")
    private String description;

    public Library(Library library) {
        name = library.getName();
        city = library.getCity();
        street = library.getStreet();
        streetNumber = library.getStreetNumber();
        description = library.getDescription();
    }

    public Library(String name, String city, String street, Integer streetNumber, String description) {
        this.name = name;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return Objects.equals(name, library.name) && Objects.equals(city, library.city) && Objects.equals(street, library.street) && Objects.equals(streetNumber, library.streetNumber) && Objects.equals(description, library.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, city, street, streetNumber, description);
    }
}
