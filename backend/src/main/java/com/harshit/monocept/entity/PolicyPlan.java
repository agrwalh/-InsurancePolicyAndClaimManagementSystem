package com.harshit.monocept.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.PremiumType;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_plans", indexes = {
		@Index(name = "idx_policy_plans_product_active", columnList = "product_id, isActive"),
		@Index(name = "idx_policy_plans_is_active", columnList = "isActive"),
		@Index(name = "idx_policy_plans_product_name", columnList = "product_id, planName") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private InsuranceProduct product;

	@Column(nullable = false)
	private String planName;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal coverageAmount;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal premiumAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PremiumType premiumType;

	@Column(nullable = false)
	private Integer durationYears;

	@Column(nullable = false, length = 2000)
	private String termsAndConditions;

	@Builder.Default
	private Boolean isActive = true;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}