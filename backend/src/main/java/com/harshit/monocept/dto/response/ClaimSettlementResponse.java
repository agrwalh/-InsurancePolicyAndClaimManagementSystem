package com.harshit.monocept.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.harshit.monocept.enums.SettlementStatus;

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
public class ClaimSettlementResponse {
	private Long settlementId;
	private String settlementNumber;
	private Long claimId;
	private String claimNumber;
	private BigDecimal approvedAmount;
	private SettlementStatus settlementStatus;
	private String paymentReference;
	private String createdBy;
	private String paidBy;
	private LocalDateTime settledAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}