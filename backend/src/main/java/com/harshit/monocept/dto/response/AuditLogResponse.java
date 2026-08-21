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
public class AuditLogResponse {
	private Long auditId;
	private Long actorUserId;
	private String actorEmail;
	private Role actorRole;
	private String action;
	private String entityType;
	private Long entityId;
	private String remarks;
	private LocalDateTime createdAt;
}