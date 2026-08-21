package com.harshit.monocept.controller;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.harshit.monocept.dto.request.ClaimDecisionRequest;
import com.harshit.monocept.dto.request.ClaimRequest;
import com.harshit.monocept.dto.request.ClaimReviewRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.ClaimHistoryResponse;
import com.harshit.monocept.dto.response.ClaimResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.dto.response.RiskAssessmentResponse;
import com.harshit.monocept.enums.ClaimStatus;
import com.harshit.monocept.service.ClaimService;
import com.harshit.monocept.service.RiskAssessmentService;
import com.harshit.monocept.util.PaginationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

	private final ClaimService claimService;
	private final RiskAssessmentService riskAssessmentService;

	@PostMapping(value = "/with-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<ClaimResponse>> submitClaimWithDocuments(
			@RequestPart("claimData") @Valid ClaimRequest req,
			@RequestPart(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "documentNames", required = false) List<String> documentNames,
			@RequestParam(value = "documentTypes", required = false) List<String> documentTypes, Authentication auth) {

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Claim submitted",
				claimService.submitClaimWithDocuments(req, files, documentNames, documentTypes, auth.getName())));
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PagedResponse<ClaimResponse>>> getMyClaims(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, Authentication auth) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.CLAIM_SORT_FIELDS);
		Page<ClaimResponse> result = claimService.getMyClaims(auth.getName(), pageable);
		return ResponseEntity.ok(ApiResponse.success("My claims", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<ClaimResponse>>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, @RequestParam(required = false) ClaimStatus status) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.CLAIM_SORT_FIELDS);

		Page<ClaimResponse> result = (status != null) ? claimService.getClaimsByStatus(status, pageable)
				: claimService.getAllClaims(pageable);

		return ResponseEntity.ok(ApiResponse.success("Claims", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/assigned-to-me")
	@PreAuthorize("hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<ClaimResponse>>> getAssignedToMe(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, Authentication auth) {
		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.CLAIM_SORT_FIELDS);
		Page<ClaimResponse> result = claimService.getAssignedToMe(auth.getName(), pageable);
		return ResponseEntity.ok(ApiResponse.success("Assigned claims", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/{claimId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<ClaimResponse>> getById(@PathVariable Long claimId, Authentication auth) {
		return ResponseEntity
				.ok(ApiResponse.success("Claim details", claimService.getClaimById(claimId, auth.getName())));
	}

	@PatchMapping("/{claimId}/review")
	@PreAuthorize("hasRole('AGENT')")
	public ResponseEntity<ApiResponse<ClaimResponse>> review(@PathVariable Long claimId,
			@Valid @RequestBody ClaimReviewRequest req, Authentication auth) {
		return ResponseEntity
				.ok(ApiResponse.success("Claim reviewed", claimService.reviewClaim(claimId, req, auth.getName())));
	}

	@PatchMapping("/{claimId}/assign/{agentId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ClaimResponse>> assign(@PathVariable Long claimId, @PathVariable Long agentId,
			Authentication auth) {
		return ResponseEntity
				.ok(ApiResponse.success("Claim assigned", claimService.assignClaim(claimId, agentId, auth.getName())));
	}

	@GetMapping("/{claimId}/risk-assessment")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<RiskAssessmentResponse>> riskAssessment(@PathVariable Long claimId) {
		return ResponseEntity.ok(ApiResponse.success("Risk assessment", riskAssessmentService.assess(claimId)));
	}

	@PatchMapping("/{claimId}/decide")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ClaimResponse>> decide(@PathVariable Long claimId,
			@Valid @RequestBody ClaimDecisionRequest req, Authentication auth) {
		return ResponseEntity.ok(
				ApiResponse.success("Claim decision recorded", claimService.decideClaim(claimId, req, auth.getName())));
	}

	@GetMapping("/{claimId}/history")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PagedResponse<ClaimHistoryResponse>>> getHistory(@PathVariable Long claimId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
			@RequestParam(defaultValue = "asc") String direction, Authentication auth) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.CLAIM_HISTORY_SORT_FIELDS);
		Page<ClaimHistoryResponse> result = claimService.getClaimHistory(claimId, auth.getName(), pageable);
		return ResponseEntity.ok(ApiResponse.success("Claim history", PagedResponse.from(result, sortBy, direction)));
	}
}