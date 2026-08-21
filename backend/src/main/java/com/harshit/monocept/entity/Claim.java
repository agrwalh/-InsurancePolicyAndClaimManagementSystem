package com.harshit.monocept.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.ClaimStatus;

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
@Table(name = "claims", indexes = { @Index(name = "idx_claims_policy_id", columnList = "policy_id"),
		@Index(name = "idx_claims_claim_status", columnList = "claimStatus"),
		@Index(name = "idx_claims_assigned_agent", columnList = "assigned_agent_id"),
		@Index(name = "idx_claims_policy_status", columnList = "policy_id, claimStatus") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String claimNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_id", nullable = false)
	private Policy policy;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal claimAmount;

	@Column(nullable = false)
	private String claimReason;

	@Column(nullable = false)
	private LocalDate incidentDate;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private ClaimStatus claimStatus = ClaimStatus.SUBMITTED;

	private String agentRemarks;
	private String adminRemarks;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_agent_id")
	private User assignedAgent;

	private LocalDateTime assignedAt;

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
