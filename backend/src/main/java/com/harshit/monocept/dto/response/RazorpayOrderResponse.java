package com.harshit.monocept.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {

	private String razorpayOrderId;
	private String razorpayKeyId; // public key, safe to expose to frontend
	private BigDecimal amount; // in rupees (for display)
	private Long amountInPaise; // what Razorpay checkout.js actually needs
	private String currency;
	private Long policyId;
	private String policyNumber;
	private String customerName;
	private String customerEmail;
	private String customerPhone;
}