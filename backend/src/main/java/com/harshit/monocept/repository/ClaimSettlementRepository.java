package com.harshit.monocept.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.ClaimSettlement;

@Repository
public interface ClaimSettlementRepository extends JpaRepository<ClaimSettlement, Long> {
	boolean existsByClaimId(Long claimId);

	boolean existsBySettlementNumber(String settlementNumber);

	Optional<ClaimSettlement> findByClaimId(Long claimId);
}