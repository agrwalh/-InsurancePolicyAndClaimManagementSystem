package com.harshit.monocept.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.CreateAgentRequest;
import com.harshit.monocept.dto.request.UserStatusUpdateRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.dto.response.UserResponse;
import com.harshit.monocept.enums.Role;
import com.harshit.monocept.service.UserService;
import com.harshit.monocept.util.PaginationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction, @RequestParam(required = false) Role role,
			@RequestParam(required = false) Boolean isActive) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, direction,
				PaginationUtil.USER_SORT_FIELDS);

		Page<UserResponse> result;
		if (role != null)
			result = userService.getUsersByRole(role, pageable);
		else if (isActive != null)
			result = userService.getUsersByStatus(isActive, pageable);
		else
			result = userService.getAllUsers(pageable);

		return ResponseEntity.ok(ApiResponse.success("Users fetched", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long userId) {
		return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(userId)));
	}

	@PostMapping("/agent")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserResponse>> createAgent(@Valid @RequestBody CreateAgentRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success("Insurance Operations Officer created successfully", userService.createAgent(req)));
	}

	@PatchMapping("/{userId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long userId,
			@Valid @RequestBody UserStatusUpdateRequest req) {
		return ResponseEntity.ok(ApiResponse.success("User status updated", userService.updateUserStatus(userId, req)));
	}
}