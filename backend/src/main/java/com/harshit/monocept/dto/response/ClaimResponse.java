package com.harshit.monocept.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.harshit.monocept.enums.ClaimStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.harshit.monocept.enums.ProductType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponse {
	private Long claimId;
	private String claimNumber;
	private Long policyId;
	private String policyNumber;
	private String planName;
	private String productType;
	private String customerName;
	private BigDecimal claimAmount;
	private String claimReason;
	private LocalDate incidentDate;
	private ClaimStatus claimStatus;
	private String agentRemarks;
	private String adminRemarks;
	private Long assignedAgentId;
	private String assignedAgentName;
	private LocalDateTime assignedAt;
	private List<DocumentResponse> documents;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DocumentResponse {
		private Long documentId;
		private String documentName;
		private String documentType;
		private String documentReference;
	}
}