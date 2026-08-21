package com.harshit.monocept.dto.response;

import java.util.List;

import com.harshit.monocept.enums.RiskLevel;

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
public class RiskAssessmentResponse {
	private Long claimId;
	private String claimNumber;
	private int riskScore;
	private RiskLevel riskLevel;
	private List<String> reasons;
}