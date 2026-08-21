package com.harshit.monocept.entity;

import java.time.LocalDateTime;


import com.harshit.monocept.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs", indexes = { @Index(name = "idx_audit_logs_entity_type", columnList = "entityType"),
		@Index(name = "idx_audit_logs_actor_user_id", columnList = "actorUserId"),
		@Index(name = "idx_audit_logs_created_at", columnList = "createdAt") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long actorUserId;
	private String actorEmail;

	@Enumerated(EnumType.STRING)
	private Role actorRole;

	@Column(nullable = false)
	private String action;

	@Column(nullable = false)
	private String entityType;

	private Long entityId;

	@Column(length = 2000)
	private String remarks;

	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}