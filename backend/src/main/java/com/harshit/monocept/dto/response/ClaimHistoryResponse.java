package com.harshit.monocept.dto.response;

import java.time.LocalDateTime;

import com.harshit.monocept.enums.ClaimStatus;

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
public class ClaimHistoryResponse {
	private Long historyId;
	private Long claimId;
	private String claimNumber;
	private ClaimStatus previousStatus;
	private ClaimStatus newStatus;
	private String remarks;
	private String updatedByName;
	private String updatedByRole;
	private LocalDateTime updatedAt;
}