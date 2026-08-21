package com.harshit.monocept.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementPaidRequest {
	@NotBlank(message = "Payment reference is required")
	private String paymentReference;
}