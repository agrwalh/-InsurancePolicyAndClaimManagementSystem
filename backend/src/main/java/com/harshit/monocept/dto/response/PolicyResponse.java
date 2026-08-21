package com.harshit.monocept.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.harshit.monocept.enums.PolicyStatus;
import com.harshit.monocept.enums.PremiumFrequency;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyResponse {
	private Long policyId;
	private String policyNumber;
	private Long customerId;
	private String customerName;
	private Long planId;
	private String planName;
	private ProductType productType;
	private BigDecimal coverageAmount;
	private BigDecimal premiumAmount;
	private PremiumType premiumType;
	private LocalDate startDate;
	private LocalDate endDate;
	private PolicyStatus status;
	private BigDecimal totalPremiumPaid;
	private Integer premiumsPaid;
	private LocalDate nextPremiumDueDate;
	private PremiumFrequency premiumFrequency;
    private BigDecimal installmentAmount;
    private Integer totalInstallments; // renamed from totalInstallmentsDue
    // Convenience field to indicate how many installments are still pending
    private Integer remainingInstallments;
	private Integer durationYears;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	// Required only when the chosen plan's product type is MOTOR
	private String vehicleRegistrationNumber;
	private String vehicleMake;
	private String vehicleModel;
	private Integer vehicleManufactureYear;
	private BigDecimal calculatedIdv;
	private Integer ncbPercentage;
	private Integer vehicleAgeAtPurchase;
	private Integer depreciationPercentApplied;

    // ================= HEALTH DISCLOSURES (optional, HEALTH product only) =================
    private String healthCoverType;
    private Integer healthInsuredAge;
    private Integer healthAdultCount;
    private Integer healthChildCount;
    private Boolean healthHasPreExisting;
    private java.util.List<Integer> healthAdultAges;
    private java.util.List<Integer> healthChildAges;
    private java.util.List<HealthPreExistingDto> healthPreExisting;

    // ================= LIFE DISCLOSURES (optional, LIFE product only) =================
    private java.time.LocalDate lifeDob;
    private Boolean lifeSmoker;
    private java.util.List<LifeNomineeDto> lifeNominees;
}