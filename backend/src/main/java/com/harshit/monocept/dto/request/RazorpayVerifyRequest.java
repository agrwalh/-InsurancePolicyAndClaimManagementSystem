package com.harshit.monocept.dto.request;

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
public class RazorpayVerifyRequest {

	@NotNull(message = "Policy ID is required")
	private Long policyId;

	@NotBlank(message = "Razorpay order id is required")
	private String razorpayOrderId;

	@NotBlank(message = "Razorpay payment id is required")
	private String razorpayPaymentId;

	@NotBlank(message = "Razorpay signature is required")
	private String razorpaySignature;
}