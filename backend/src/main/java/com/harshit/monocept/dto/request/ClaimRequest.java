package com.harshit.monocept.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

	@NotNull(message = "Policy ID is required")
	private Long policyId;

	@NotNull(message = "Claim amount is required")
	@DecimalMin(value = "0.01", message = "Claim amount must be greater than 0")
	private BigDecimal claimAmount;

	@NotBlank(message = "Claim reason is required")
	private String claimReason;

	@NotNull(message = "Incident date is required")
	@PastOrPresent(message = "Incident date cannot be a future date")
	private LocalDate incidentDate;
}