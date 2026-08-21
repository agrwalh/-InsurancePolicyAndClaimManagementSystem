package com.harshit.monocept.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.PremiumType;
import com.harshit.monocept.enums.ProductType;

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
public class PlanResponse {
	private Long planId;
	private Long productId;
	private String productName;
	private ProductType productType;
	private String planName;
	private BigDecimal coverageAmount;
	private BigDecimal premiumAmount;
	private PremiumType premiumType;
	private Integer durationYears;
	private String termsAndConditions;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private BigDecimal calculatedIdv;
	private Integer vehicleAgeAtPurchase;
	private Integer depreciationPercentApplied;
	private Integer ncbPercentage;
}