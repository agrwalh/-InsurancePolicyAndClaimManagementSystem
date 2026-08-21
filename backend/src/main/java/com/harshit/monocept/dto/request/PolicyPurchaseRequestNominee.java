package com.harshit.monocept.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseRequestNominee {
    @NotBlank
    private String name;

    @NotBlank
    private String relationship;
    @Min(1)
    @Max(100)
    private Integer sharePct;

    private LocalDate dob; 
}
