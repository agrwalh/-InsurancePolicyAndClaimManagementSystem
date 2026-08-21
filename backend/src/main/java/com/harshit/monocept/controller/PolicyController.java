package com.harshit.monocept.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.PolicyIssueRequest;
import com.harshit.monocept.dto.request.PolicyPurchaseRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.dto.response.PolicyResponse;
import com.harshit.monocept.service.PolicyService;
import com.harshit.monocept.util.PaginationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

	private final PolicyService policyService;

	@PostMapping("/purchase")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PolicyResponse>> purchase(@Valid @RequestBody PolicyPurchaseRequest req,
			Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Policy purchased successfully",
				policyService.purchasePolicy(req, auth.getName())));
	}

	@PostMapping("/issue")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PolicyResponse>> issue(@Valid @RequestBody PolicyIssueRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Policy issued successfully", policyService.issuePolicy(req)));
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PagedResponse<PolicyResponse>>> getMyPolicies(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, Authentication auth) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.POLICY_SORT_FIELDS);
		Page<PolicyResponse> result = policyService.getMyPolicies(auth.getName(), pageable);
		return ResponseEntity.ok(ApiResponse.success("My policies", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<PolicyResponse>>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.POLICY_SORT_FIELDS);
		Page<PolicyResponse> result = policyService.getAllPolicies(pageable);
		return ResponseEntity.ok(ApiResponse.success("All policies", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<PolicyResponse>>> getByCustomer(@PathVariable Long customerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.POLICY_SORT_FIELDS);
		Page<PolicyResponse> result = policyService.getPoliciesByCustomer(customerId, pageable);
		return ResponseEntity
				.ok(ApiResponse.success("Customer policies", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/{policyId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PolicyResponse>> getById(@PathVariable Long policyId, Authentication auth) {
		return ResponseEntity
				.ok(ApiResponse.success("Policy details", policyService.getPolicyById(policyId, auth.getName())));
	}

	@PatchMapping("/{policyId}/cancel")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PolicyResponse>> cancel(@PathVariable Long policyId, Authentication auth) {
		return ResponseEntity
				.ok(ApiResponse.success("Policy cancelled", policyService.cancelPolicy(policyId, auth.getName())));
	}
}