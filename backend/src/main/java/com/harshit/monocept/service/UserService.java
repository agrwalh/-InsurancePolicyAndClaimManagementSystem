package com.harshit.monocept.service;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.harshit.monocept.dto.request.CreateAgentRequest;
import com.harshit.monocept.dto.request.UserStatusUpdateRequest;
import com.harshit.monocept.dto.response.UserResponse;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.OtpChannel;
import com.harshit.monocept.enums.Role;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.DuplicateResourceException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public Page<UserResponse> getAllUsers(Pageable pageable) {
		log.debug("Fetching all users, page: {}", pageable.getPageNumber());
		return userRepository.findAll(pageable).map(this::mapToResponse);
	}

	public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
		log.debug("Fetching users by role: {}", role);
		return userRepository.findByRole(role, pageable).map(this::mapToResponse);
	}

	public Page<UserResponse> getUsersByStatus(Boolean isActive, Pageable pageable) {
		log.debug("Fetching users by isActive: {}", isActive);
		return userRepository.findByIsActive(isActive, pageable).map(this::mapToResponse);
	}

	public UserResponse createAgent(CreateAgentRequest req) {
		log.info("Insurance Operations Officer creation attempt: email={}", req.getEmail());

		if (userRepository.existsByEmail(req.getEmail())) {
			log.warn("Duplicate email on insurance operations officer creation: {}", req.getEmail());
			throw new DuplicateResourceException("Email already exists: " + req.getEmail());
		}

		User agent = User.builder().fullName(req.getFullName()).email(req.getEmail())
				.password(passwordEncoder.encode(req.getPassword())).mobileNumber(req.getMobileNumber())
				.role(Role.AGENT).preferredOtpChannel(OtpChannel.EMAIL).isActive(true).isVerified(true)
				.emailVerified(true).phoneVerified(true).build();

		User saved = userRepository.save(agent);
		log.info("Insurance Operations Officer created: id={}, email={}", saved.getId(), saved.getEmail());
		return mapToResponse(saved);
	}

	public UserResponse updateUserStatus(Long userId, UserStatusUpdateRequest req) {
		log.info("User status update: userId={}, newStatus={}", userId, req.getIsActive());

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		if (user.getRole() == Role.ADMIN && !req.getIsActive()) {
			log.warn("Attempt to deactivate admin: userId={}", userId);
			throw new BusinessRuleException("Cannot deactivate an admin account");
		}

		Boolean oldStatus = user.getIsActive();
		user.setIsActive(req.getIsActive());
		User updated = userRepository.save(user);

		log.info("User status changed: userId={}, {} -> {}, reason={}", userId, oldStatus, req.getIsActive(),
				req.getReason());

		return mapToResponse(updated);
	}

	public UserResponse getUserById(Long userId) {
		log.debug("Fetching user by id: {}", userId);
		return mapToResponse(userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)));
	}

	private UserResponse mapToResponse(User u) {
		return UserResponse.builder().userId(u.getId()).fullName(u.getFullName()).email(u.getEmail())
				.mobileNumber(u.getMobileNumber()).role(u.getRole()).isActive(u.getIsActive())
				.createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt()).build();
	}
}