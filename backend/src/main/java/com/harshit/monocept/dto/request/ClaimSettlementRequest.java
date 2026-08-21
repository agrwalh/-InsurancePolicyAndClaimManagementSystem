package com.harshit.monocept.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimSettlementRequest {
	@NotNull(message = "Approved amount is required")
	@DecimalMin(value = "0.01", message = "Approved amount must be greater than zero")
	private BigDecimal approvedAmount;
}