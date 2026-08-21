package com.harshit.monocept.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.PolicyPlan;

@Repository
public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {
	Page<PolicyPlan> findByProductIdAndIsActiveTrue(Long productId, Pageable pageable);

	Page<PolicyPlan> findByIsActiveTrue(Pageable pageable);

	boolean existsByProductIdAndPlanNameIgnoreCase(Long productId, String planName);

	boolean existsByProductIdAndPlanNameIgnoreCaseAndIdNot(Long productId, String planName, Long id);
}