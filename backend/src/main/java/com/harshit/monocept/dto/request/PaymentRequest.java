package com.harshit.monocept.dto.request;

import java.math.BigDecimal;

import com.harshit.monocept.enums.PaymentMode;
import com.harshit.monocept.enums.PaymentStatus;

import jakarta.validation.constraints.DecimalMin;
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
public class PaymentRequest {

	@NotNull(message = "Policy ID is required")
	private Long policyId;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	private BigDecimal amount;

	@NotNull(message = "Payment mode is required")
	private PaymentMode paymentMode;

	@NotBlank(message = "Transaction reference is required")
	private String transactionReference;

	@NotNull(message = "Payment status is required")
	private PaymentStatus paymentStatus;

	private String razorpayOrderId;

	private String razorpayPaymentId;

	private String razorpaySignature;
}