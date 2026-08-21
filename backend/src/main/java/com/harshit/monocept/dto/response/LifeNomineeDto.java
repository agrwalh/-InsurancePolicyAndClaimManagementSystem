package com.harshit.monocept.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LifeNomineeDto {
    private String name;
    private String relationship;
    private Integer sharePct;
    private LocalDate dob;
}
