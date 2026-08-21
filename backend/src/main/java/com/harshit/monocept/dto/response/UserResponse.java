package com.harshit.monocept.dto.response;

import java.time.LocalDateTime;

import com.harshit.monocept.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
	private Long userId;
	private String fullName;
	private String email;
	private String mobileNumber;
	private Role role;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}