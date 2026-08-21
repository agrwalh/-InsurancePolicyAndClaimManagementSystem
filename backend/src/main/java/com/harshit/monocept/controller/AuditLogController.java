package com.harshit.monocept.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.AuditLogResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.service.AuditService;
import com.harshit.monocept.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

	private final AuditService auditService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAll(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, @RequestParam(required = false) String entityType,
			@RequestParam(required = false) Long actorUserId) {
		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.AUDIT_SORT_FIELDS);
		Page<AuditLogResponse> result = actorUserId != null ? auditService.getByActor(actorUserId, pageable)
				: entityType != null ? auditService.getByEntityType(entityType, pageable)
						: auditService.getAll(pageable);
		return ResponseEntity.ok(ApiResponse.success("Audit logs", PagedResponse.from(result, sortBy, direction)));
	}
}