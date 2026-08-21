package com.harshit.monocept.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.PaymentMode;
import com.harshit.monocept.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "premium_payments", indexes = {
		@Index(name = "idx_premium_payments_policy_id", columnList = "policy_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_id", nullable = false)
	private Policy policy;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	private LocalDateTime paymentDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMode paymentMode;

	@Column(nullable = false, unique = true)
	private String transactionReference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus;

	private LocalDateTime createdAt;

	private String razorpayOrderId;

	private String razorpayPaymentId;

	@Column(length = 500)
	private String razorpaySignature;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		paymentDate = LocalDateTime.now();
	}
}