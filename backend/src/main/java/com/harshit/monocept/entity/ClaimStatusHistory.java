package com.harshit.monocept.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "claim_status_history", indexes = {
		@Index(name = "idx_claim_status_history_claim_id", columnList = "claim_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "claim_id", nullable = false)
	private Claim claim;

	@Enumerated(EnumType.STRING)
	private ClaimStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ClaimStatus newStatus;

	private String remarks;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by", nullable = false)
	private User updatedBy;

	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		updatedAt = LocalDateTime.now();
	}
}
