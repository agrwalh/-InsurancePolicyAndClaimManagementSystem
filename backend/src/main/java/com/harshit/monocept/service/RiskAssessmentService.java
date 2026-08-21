package com.harshit.monocept.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.harshit.monocept.dto.response.RiskAssessmentResponse;
import com.harshit.monocept.entity.Claim;
import com.harshit.monocept.enums.RiskLevel;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.ClaimRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

	private final ClaimRepository claimRepository;

	public RiskAssessmentResponse assess(Long claimId) {
		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
		int score = 0;
		List<String> reasons = new ArrayList<>();

		long daysFromStart = ChronoUnit.DAYS.between(claim.getPolicy().getStartDate(), claim.getIncidentDate());
		if (daysFromStart >= 0 && daysFromStart <= 15) {
			score += 35;
			reasons.add("Incident happened within 15 days of policy start");
		}

		BigDecimal coverage = claim.getPolicy().getPlan().getCoverageAmount();
		if (coverage.signum() > 0
				&& claim.getClaimAmount().compareTo(coverage.multiply(BigDecimal.valueOf(0.80))) >= 0) {
			score += 30;
			reasons.add("Claim amount is 80% or more of coverage amount");
		}

		long previousClaims = claimRepository.countByPolicyCustomerIdAndIdNot(claim.getPolicy().getCustomer().getId(),
				claimId);
		if (previousClaims >= 2) {
			score += 25;
			reasons.add("Customer has multiple previous claims");
		} else if (previousClaims == 1) {
			score += 10;
			reasons.add("Customer has one previous claim");
		}

		RiskLevel level = score >= 60 ? RiskLevel.HIGH : score >= 30 ? RiskLevel.MEDIUM : RiskLevel.LOW;
		if (reasons.isEmpty())
			reasons.add("No major risk indicators found");

		return RiskAssessmentResponse.builder().claimId(claim.getId()).claimNumber(claim.getClaimNumber())
				.riskScore(Math.min(score, 100)).riskLevel(level).reasons(reasons).build();
	}
}