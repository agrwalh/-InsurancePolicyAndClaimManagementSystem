package com.harshit.monocept.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.harshit.monocept.enums.PolicyStatus;
import com.harshit.monocept.enums.PremiumFrequency;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policies", indexes = { @Index(name = "idx_policies_customer_id", columnList = "customer_id"),
		@Index(name = "idx_policies_status", columnList = "status"),
		@Index(name = "idx_policies_customer_status", columnList = "customer_id, status"),
		@Index(name = "idx_policies_status_next_premium", columnList = "status, nextPremiumDueDate"),
		@Index(name = "idx_policies_customer_plan_status", columnList = "customer_id, plan_id, status"),
		@Index(name = "idx_policies_vehicle_reg_status", columnList = "vehicleRegistrationNumber, status") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String policyNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id", nullable = false)
	private PolicyPlan plan;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PolicyStatus status = PolicyStatus.PENDING_PAYMENT;

	@Builder.Default
	@Column(precision = 15, scale = 2)
	private BigDecimal totalPremiumPaid = BigDecimal.ZERO;

	@Builder.Default
	private Integer premiumsPaid = 0;

	private LocalDate nextPremiumDueDate;

	@Enumerated(EnumType.STRING)
	private PremiumFrequency premiumFrequency;

	@Column(precision = 15, scale = 2)
	private BigDecimal installmentAmount;

	private Integer totalInstallmentsDue;

	private LocalDate lapsedAt;

	private String vehicleRegistrationNumber;
	private String vehicleMake;
	private String vehicleModel;
	private Integer vehicleManufactureYear;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private BigDecimal calculatedIdv;
	private Integer vehicleAgeAtPurchase;
	private Integer depreciationPercentApplied;

	@Builder.Default
	private Integer ncbPercentage = 0;

	// ================= HEALTH DISCLOSURES (optional, HEALTH product only)
	// =================
	private String healthCoverType; // INDIVIDUAL | FLOATER
	private Integer healthInsuredAge; // INDIVIDUAL
	private Integer healthAdultCount; // FLOATER
	private Integer healthChildCount; // FLOATER
	private Boolean healthHasPreExisting;

	@ElementCollection
	@CollectionTable(name = "policy_health_adult_ages", joinColumns = @JoinColumn(name = "policy_id"))
	@OrderColumn(name = "idx")
	@Column(name = "age")
	private List<Integer> healthAdultAges;

	@ElementCollection
	@CollectionTable(name = "policy_health_child_ages", joinColumns = @JoinColumn(name = "policy_id"))
	@OrderColumn(name = "idx")
	@Column(name = "age")
	private List<Integer> healthChildAges;

	@ElementCollection
	@CollectionTable(name = "policy_health_preexisting", joinColumns = @JoinColumn(name = "policy_id"))
	@OrderColumn(name = "idx")
	private List<HealthPreExistingEntry> healthPreExisting;

	// ================= LIFE DISCLOSURES (optional, LIFE product only)
	// =================
	private java.time.LocalDate lifeDob;
	private Boolean lifeSmoker;

	@ElementCollection
	@CollectionTable(name = "policy_life_nominees", joinColumns = @JoinColumn(name = "policy_id"))
	@OrderColumn(name = "idx")
	private java.util.List<LifeNomineeEntry> lifeNominees;

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