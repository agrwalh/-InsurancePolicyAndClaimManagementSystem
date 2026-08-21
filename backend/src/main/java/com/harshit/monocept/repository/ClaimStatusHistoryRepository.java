package com.harshit.monocept.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.ClaimStatusHistory;

@Repository
public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {
	Page<ClaimStatusHistory> findByClaimId(Long claimId, Pageable pageable);
}