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
    Integer authorId;

    private @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    Integer bookId;

    private Integer authorshipOrder;

    public Authorship(Authorship authorship) {
        authorshipOrder = authorship.getAuthorshipOrder();
    }

    public Authorship(Integer authorshipOrder) {
        this.authorshipOrder = authorshipOrder;
    }
}
