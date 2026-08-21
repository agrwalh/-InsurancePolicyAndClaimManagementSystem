package com.harshit.monocept.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.harshit.monocept.dto.response.AuditLogResponse;
import com.harshit.monocept.entity.AuditLog;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;

	public void record(User actor, String action, String entityType, Long entityId, String remarks) {
		auditLogRepository.save(AuditLog.builder().actorUserId(actor != null ? actor.getId() : null)
				.actorEmail(actor != null ? actor.getEmail() : null).actorRole(actor != null ? actor.getRole() : null)
				.action(action).entityType(entityType).entityId(entityId).remarks(remarks).build());
	}

	public Page<AuditLogResponse> getAll(Pageable pageable) {
		return auditLogRepository.findAll(pageable).map(this::mapToResponse);
	}

	public Page<AuditLogResponse> getByEntityType(String entityType, Pageable pageable) {
		return auditLogRepository.findByEntityType(entityType, pageable).map(this::mapToResponse);
	}

	public Page<AuditLogResponse> getByActor(Long actorUserId, Pageable pageable) {
		return auditLogRepository.findByActorUserId(actorUserId, pageable).map(this::mapToResponse);
	}

	private AuditLogResponse mapToResponse(AuditLog a) {
		return AuditLogResponse.builder().auditId(a.getId()).actorUserId(a.getActorUserId())
				.actorEmail(a.getActorEmail()).actorRole(a.getActorRole()).action(a.getAction())
				.entityType(a.getEntityType()).entityId(a.getEntityId()).remarks(a.getRemarks())
				.createdAt(a.getCreatedAt()).build();
	}
}