package com.harshit.monocept.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.PaymentMode;
import com.harshit.monocept.enums.PaymentStatus;

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
public class PaymentResponse {
	private Long paymentId;
	private Long policyId;
	private String policyNumber;
	private BigDecimal amount;
	private LocalDateTime paymentDate;
	private PaymentMode paymentMode;
	private String transactionReference;
	private PaymentStatus paymentStatus;
	private LocalDateTime createdAt;
}