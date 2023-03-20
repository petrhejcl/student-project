package com.redhat.restdemo.model.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "genre")
@Getter
@Setter
@NoArgsConstructor
public class Genre {
    private @Id
    @Column(name = "name")
    String name;

    public Genre(String name) {
        this.name = name;
    }
    public Genre(Genre genre) {
        name = genre.getName();
    }
}
