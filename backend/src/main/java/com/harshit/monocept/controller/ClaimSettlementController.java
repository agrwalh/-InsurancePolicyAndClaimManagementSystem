package com.harshit.monocept.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.ClaimSettlementRequest;
import com.harshit.monocept.dto.request.SettlementPaidRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.ClaimSettlementResponse;
import com.harshit.monocept.service.ClaimSettlementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class ClaimSettlementController {

	private final ClaimSettlementService settlementService;

	@PostMapping("/claim/{claimId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ClaimSettlementResponse>> initiate(@PathVariable Long claimId,
			@Valid @RequestBody ClaimSettlementRequest req, Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Settlement initiated", settlementService.initiate(claimId, req, auth.getName())));
	}

	@PatchMapping("/{settlementId}/paid")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ClaimSettlementResponse>> markPaid(@PathVariable Long settlementId,
			@Valid @RequestBody SettlementPaidRequest req, Authentication auth) {
		return ResponseEntity.ok(ApiResponse.success("Settlement marked paid",
				settlementService.markPaid(settlementId, req, auth.getName())));
	}

	@GetMapping("/{settlementId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<ClaimSettlementResponse>> getById(@PathVariable Long settlementId) {
		return ResponseEntity.ok(ApiResponse.success("Settlement fetched", settlementService.getById(settlementId)));
	}

	@GetMapping("/claim/{claimId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<ClaimSettlementResponse>> getByClaim(@PathVariable Long claimId, Authentication auth) {
		return ResponseEntity.ok(ApiResponse.success("Settlement fetched", settlementService.getByClaim(claimId, auth.getName())));
	}
}