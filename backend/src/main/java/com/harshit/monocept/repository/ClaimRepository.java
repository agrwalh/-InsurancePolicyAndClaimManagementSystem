package com.harshit.monocept.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.Claim;
import com.harshit.monocept.enums.ClaimStatus;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
	Page<Claim> findByPolicyCustomerId(Long customerId, Pageable pageable);

	Page<Claim> findByClaimStatus(ClaimStatus status, Pageable pageable);

	Page<Claim> findByAssignedAgentId(Long agentId, Pageable pageable);

	long countByPolicyCustomerIdAndIdNot(Long customerId, Long claimId);

	@Query("""
			select coalesce(sum(
				case
					when s.approvedAmount is not null then s.approvedAmount
					else c.claimAmount
				end
			), 0)
			from Claim c
			left join ClaimSettlement s on s.claim.id = c.id
			where c.policy.id = :policyId
			  and c.claimStatus <> com.harshit.monocept.enums.ClaimStatus.REJECTED
			""")
	BigDecimal sumNonRejectedClaimAmountByPolicyId(@Param("policyId") Long policyId);

	boolean existsByClaimNumber(String claimNumber);
}