package com.harshit.monocept.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
	private Long customerId;
	private String fullName;
	private String email;
	private String mobileNumber;
	private LocalDate dateOfBirth;
	private String address;
	private String city;
	private String state;
	private String pinCode;
	private String nomineeName;
	private String nomineeRelation;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}