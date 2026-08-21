package com.harshit.monocept.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "claim_documents", indexes = { @Index(name = "idx_claim_documents_claim_id", columnList = "claim_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "claim_id", nullable = false)
	private Claim claim;

	@Column(nullable = false)
	private String documentName;

	@Column(nullable = false)
	private String documentType;

	@Column(nullable = false)
	private String documentReference;

	@Column
	private String cloudinaryPublicId;

	@Column
	private String originalFileName;

	@Column
	private Long fileSizeBytes;

	@Column
	private String fileFormat;

	private LocalDateTime uploadedAt;

	@PrePersist
	protected void onCreate() {
		uploadedAt = LocalDateTime.now();
	}
}