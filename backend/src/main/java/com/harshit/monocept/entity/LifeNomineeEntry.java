package com.harshit.monocept.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LifeNomineeEntry {
    @Column(name = "name")
    private String name;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "share_pct")
    private Integer sharePct;

    @Column(name = "dob")
    private LocalDate dob;
}
