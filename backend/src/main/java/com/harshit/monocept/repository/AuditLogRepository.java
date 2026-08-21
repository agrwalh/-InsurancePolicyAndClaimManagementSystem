package com.harshit.monocept.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

	Page<AuditLog> findByActorUserId(Long actorUserId, Pageable pageable);
}