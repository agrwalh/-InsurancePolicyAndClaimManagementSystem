package com.harshit.monocept.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.harshit.monocept.enums.PremiumFrequency;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseRequest {

	@NotNull(message = "Plan ID is required")
	private Long planId;

	@NotNull(message = "Start date is required")
	@FutureOrPresent(message = "Start date cannot be in the past")
	private LocalDate startDate;

	// Required only when the chosen plan's premiumType is ANNUAL
	private PremiumFrequency premiumFrequency;

	// Required only when the chosen plan's product type is MOTOR
	private String vehicleRegistrationNumber;
	private String vehicleMake;
	private String vehicleModel;
	private Integer vehicleManufactureYear;

    // Travel-only (optional in DTO, validated when product type is TRAVEL)
    private LocalDate tripEndDate;
    private Integer travellersAdultCount;
    private Integer travellersChildCount;

    // Health-only (all optional here; validated when product type is HEALTH)
    private String healthCoverType; // INDIVIDUAL | FLOATER
    private Integer healthInsuredAge; // INDIVIDUAL
    private Integer healthAdultCount; // FLOATER
    private Integer healthChildCount; // FLOATER
    private List<Integer> healthAdultAges; // FLOATER
    private List<Integer> healthChildAges; // FLOATER (optional when childCount=0)
    private Boolean healthHasPreExisting; // Yes/No
    private List<String> healthPreExistingConditions; // optional list of codes
    private List<Integer> healthPreExistingSinceYears; // optional aligned with conditions (nullable entries allowed)

    // Life-only (all optional here; validated when product type is LIFE)
    private LocalDate lifeDob; // used to compute age 18–65
    private Boolean lifeSmoker; // true/false
    private String lifeOccupationRisk; // LOW | MEDIUM | HIGH
    private List<PolicyPurchaseRequestNominee> lifeNominees; // at least 1 with total share=100
}
