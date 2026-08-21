package com.harshit.monocept.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderRequest {

	@NotNull(message = "Policy ID is required")
	private Long policyId;
}