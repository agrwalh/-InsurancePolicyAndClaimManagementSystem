package com.harshit.monocept.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.Policy;
import com.harshit.monocept.enums.PolicyStatus;
import com.harshit.monocept.enums.ProductType;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
	Optional<Policy> findByPolicyNumber(String policyNumber);

	boolean existsByPolicyNumber(String policyNumber);

	Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

	Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);


	List<Policy> findByStatusAndNextPremiumDueDateIsNotNull(PolicyStatus status);

	List<Policy> findByCustomerIdAndPlanIdAndStatusIn(Long customerId, Long planId, List<PolicyStatus> statuses);

	int countByCustomerIdAndStatus(Long customerId, PolicyStatus status);

	int countByCustomerIdAndPlanProductProductTypeAndStatusIn(Long customerId, ProductType productType,
			List<PolicyStatus> statuses);

	boolean existsByVehicleRegistrationNumberAndStatusIn(String vehicleRegistrationNumber, List<PolicyStatus> statuses);
}