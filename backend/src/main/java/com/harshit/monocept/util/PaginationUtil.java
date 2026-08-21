package com.harshit.monocept.util;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

	public static final int MAX_PAGE_SIZE = 100;

	public static final Set<String> USER_SORT_FIELDS = Set.of("id", "fullName", "email", "role", "isActive",
			"createdAt");
	public static final Set<String> CUSTOMER_SORT_FIELDS = Set.of("id", "city", "state", "createdAt", "updatedAt");
	public static final Set<String> PRODUCT_SORT_FIELDS = Set.of("id", "productName", "productType", "isActive",
			"createdAt", "updatedAt");
	public static final Set<String> PLAN_SORT_FIELDS = Set.of("id", "planName", "coverageAmount", "premiumAmount",
			"premiumType", "durationYears", "isActive", "createdAt", "updatedAt");
	public static final Set<String> POLICY_SORT_FIELDS = Set.of("id", "policyNumber", "startDate", "endDate", "status",
			"totalPremiumPaid", "createdAt", "updatedAt");
	public static final Set<String> PAYMENT_SORT_FIELDS = Set.of("id", "amount", "paymentDate", "paymentMode",
			"paymentStatus", "createdAt");
	public static final Set<String> CLAIM_SORT_FIELDS = Set.of("id", "claimNumber", "claimAmount", "incidentDate",
			"claimStatus", "createdAt", "updatedAt");
	public static final Set<String> CLAIM_HISTORY_SORT_FIELDS = Set.of("id", "previousStatus", "newStatus",
			"updatedAt");
	public static final Set<String> AUDIT_SORT_FIELDS = Set.of("id", "actorUserId", "actorRole", "action", "entityType",
			"createdAt");

	private PaginationUtil() {
	}

	public static Pageable createPageable(int page, int size, String sortBy, String direction,
			Set<String> allowedSortFields) {
		if (page < 0) {
			throw new IllegalArgumentException("Page number cannot be negative");
		}
		if (size <= 0) {
			throw new IllegalArgumentException("Page size must be greater than zero");
		}
		if (size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Page size cannot exceed " + MAX_PAGE_SIZE);
		}
		if (sortBy == null || sortBy.isBlank()) {
			throw new IllegalArgumentException("Sort field is required");
		}
		if (!allowedSortFields.contains(sortBy)) {
			throw new IllegalArgumentException(
					"Invalid sort field: " + sortBy + ". Allowed fields: " + allowedSortFields);
		}
		if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
			throw new IllegalArgumentException("Sort direction must be either asc or desc");
		}

		Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		return PageRequest.of(page, size, sort);
	}
}