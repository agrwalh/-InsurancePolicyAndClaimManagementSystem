package com.harshit.monocept.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harshit.monocept.dto.request.ClaimSettlementRequest;
import com.harshit.monocept.dto.request.SettlementPaidRequest;
import com.harshit.monocept.dto.response.ClaimSettlementResponse;
import com.harshit.monocept.entity.Claim;
import com.harshit.monocept.entity.ClaimSettlement;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.ClaimStatus;
import com.harshit.monocept.enums.Role;
import com.harshit.monocept.enums.SettlementStatus;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.DuplicateResourceException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.ClaimRepository;
import com.harshit.monocept.repository.ClaimSettlementRepository;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ClaimSettlementService {

	private final ClaimSettlementRepository settlementRepository;
	private final ClaimRepository claimRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	@Transactional
	public ClaimSettlementResponse initiate(Long claimId, ClaimSettlementRequest req, String adminEmail) {
		User admin = getUser(adminEmail);
		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
		if (claim.getClaimStatus() != ClaimStatus.APPROVED) {
			throw new BusinessRuleException("Settlement can be initiated only for APPROVED claims");
		}
		if (settlementRepository.existsByClaimId(claimId)) {
			throw new DuplicateResourceException("Settlement already exists for this claim");
		}
		if (req.getApprovedAmount().compareTo(claim.getClaimAmount()) > 0) {
			throw new BusinessRuleException("Approved amount cannot exceed claim amount");
		}

		ClaimSettlement saved = settlementRepository.save(ClaimSettlement.builder()
				.settlementNumber(generateSettlementNumber()).claim(claim).approvedAmount(req.getApprovedAmount())
				.settlementStatus(SettlementStatus.INITIATED).createdBy(admin).build());
		auditService.record(admin, "SETTLEMENT_INITIATED", "CLAIM_SETTLEMENT", saved.getId(),
				"Settlement initiated for claim " + claim.getClaimNumber());
		return mapToResponse(saved);
	}

	@Transactional
	public ClaimSettlementResponse markPaid(Long settlementId, SettlementPaidRequest req, String adminEmail) {
		User admin = getUser(adminEmail);
		ClaimSettlement s = settlementRepository.findById(settlementId)
				.orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + settlementId));
		if (s.getSettlementStatus() == SettlementStatus.PAID) {
			throw new BusinessRuleException("Settlement is already marked as PAID");
		}
		s.setSettlementStatus(SettlementStatus.PAID);
		s.setPaymentReference(req.getPaymentReference());
		s.setPaidBy(admin);
		s.setSettledAt(LocalDateTime.now());
		ClaimSettlement saved = settlementRepository.save(s);
		auditService.record(admin, "SETTLEMENT_PAID", "CLAIM_SETTLEMENT", saved.getId(), "Settlement paid");
		return mapToResponse(saved);
	}

	public ClaimSettlementResponse getById(Long settlementId) {
		return mapToResponse(settlementRepository.findById(settlementId)
				.orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + settlementId)));
	}

	public ClaimSettlementResponse getByClaim(Long claimId) {
		return mapToResponse(settlementRepository.findByClaimId(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Settlement not found for claim id: " + claimId)));
	}

	public ClaimSettlementResponse getByClaim(Long claimId, String requesterEmail) {
		User requester = getUser(requesterEmail);
		ClaimSettlement settlement = settlementRepository.findByClaimId(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Settlement not found for claim id: " + claimId));

		if (requester.getRole() == Role.CUSTOMER) {
			Long ownerUserId = settlement.getClaim().getPolicy().getCustomer().getUser().getId();
			if (!ownerUserId.equals(requester.getId())) {
				throw new AccessDeniedException("You are not allowed to view settlement for this claim");
			}
		}

		return mapToResponse(settlement);
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private String generateSettlementNumber() {
		String number;
		do {
			number = "SET-" + System.currentTimeMillis() + "-"
					+ UUID.randomUUID().toString().substring(0, 4).toUpperCase();
		} while (settlementRepository.existsBySettlementNumber(number));
		return number;
	}

	private ClaimSettlementResponse mapToResponse(ClaimSettlement s) {
		return ClaimSettlementResponse.builder().settlementId(s.getId()).settlementNumber(s.getSettlementNumber())
				.claimId(s.getClaim().getId()).claimNumber(s.getClaim().getClaimNumber())
				.approvedAmount(s.getApprovedAmount()).settlementStatus(s.getSettlementStatus())
				.paymentReference(s.getPaymentReference()).createdBy(s.getCreatedBy().getFullName())
				.paidBy(s.getPaidBy() != null ? s.getPaidBy().getFullName() : null).settledAt(s.getSettledAt())
				.createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
	}
}