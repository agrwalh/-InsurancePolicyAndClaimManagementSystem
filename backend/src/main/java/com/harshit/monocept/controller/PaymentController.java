package com.harshit.monocept.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.PaymentRequest;
import com.harshit.monocept.dto.request.RazorpayOrderRequest;
import com.harshit.monocept.dto.request.RazorpayVerifyRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.dto.response.PaymentResponse;
import com.harshit.monocept.dto.response.RazorpayOrderResponse;
import com.harshit.monocept.service.PaymentService;
import com.harshit.monocept.util.PaginationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')") 
	public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody PaymentRequest req,
			Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Payment recorded", paymentService.recordPayment(req, auth.getName())));
	}

	@PostMapping("/razorpay/create-order")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(
			@Valid @RequestBody RazorpayOrderRequest req, Authentication auth) {
		return ResponseEntity.ok(ApiResponse.success("Razorpay order created",
				paymentService.initiateGatewayPayment(req.getPolicyId(), auth.getName())));
	}

	@PostMapping("/razorpay/verify")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PaymentResponse>> verifyRazorpayPayment(
			@Valid @RequestBody RazorpayVerifyRequest req, Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment verified and recorded",
				paymentService.verifyGatewayPayment(req, auth.getName())));
	}

	@PostMapping("/admin")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PaymentResponse>> recordByAdmin(@Valid @RequestBody PaymentRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				"Payment recorded by admin/insurance operations officer", paymentService.recordPaymentByAdmin(req)));
	}

	@GetMapping("/my/{policyId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getMyPayments(@PathVariable Long policyId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, Authentication auth) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.PAYMENT_SORT_FIELDS);
		Page<PaymentResponse> result = paymentService.getMyPayments(policyId, auth.getName(), pageable);
		return ResponseEntity.ok(ApiResponse.success("My payments", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getAll(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.PAYMENT_SORT_FIELDS);
		Page<PaymentResponse> result = paymentService.getAllPayments(pageable);
		return ResponseEntity.ok(ApiResponse.success("All payments", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/policy/{policyId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getByPolicy(@PathVariable Long policyId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.PAYMENT_SORT_FIELDS);
		Page<PaymentResponse> result = paymentService.getPaymentsByPolicy(policyId, pageable);
		return ResponseEntity.ok(ApiResponse.success("Policy payments", PagedResponse.from(result, sortBy, direction)));
	}
}