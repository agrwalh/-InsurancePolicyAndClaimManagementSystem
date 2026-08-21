package com.harshit.monocept.dto.request;

import java.math.BigDecimal;

import com.harshit.monocept.enums.PremiumType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

	@NotNull(message = "Product ID is required")
	private Long productId;

	@NotBlank(message = "Plan name is required")
	private String planName;

	@NotNull(message = "Coverage amount is required")
	@DecimalMin(value = "0.01", message = "Coverage amount must be greater than 0")
	private BigDecimal coverageAmount;

	@NotNull(message = "Premium amount is required")
	@DecimalMin(value = "0.01", message = "Premium amount must be greater than 0")
	private BigDecimal premiumAmount;

	@NotNull(message = "Premium type is required")
	private PremiumType premiumType;

	@NotNull(message = "Duration is required")
	@Min(value = 1, message = "Duration must be at least 1 year")
	private Integer durationYears;

	@NotBlank(message = "Terms and conditions are required")
	private String termsAndConditions;

	private Boolean isActive = true;
}