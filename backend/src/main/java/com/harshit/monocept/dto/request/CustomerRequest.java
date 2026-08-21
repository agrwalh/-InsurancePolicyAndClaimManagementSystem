package com.harshit.monocept.dto.request;

import java.time.LocalDate;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be a past date")
	private LocalDate dateOfBirth;

	@AssertTrue(message = "User must be at least 18 years old")
	public boolean isAdult() {

		if (dateOfBirth == null) {
			return true;
			
		}

		return !dateOfBirth.plusYears(18).isAfter(LocalDate.now());
	}

	@NotBlank(message = "Address is required")
	private String address;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "State is required")
	private String state;

	@NotBlank(message = "PIN code is required")
	@Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code")
	private String pinCode;

	@NotBlank(message = "Nominee name is required")
	private String nomineeName;

	@NotBlank(message = "Nominee relation is required")
	private String nomineeRelation;
}