package com.harshit.monocept.service;

import java.time.LocalDate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harshit.monocept.entity.Policy;
import com.harshit.monocept.enums.PolicyStatus;
import com.harshit.monocept.repository.PolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyLapseService {

	private static final Logger log = LoggerFactory.getLogger(PolicyLapseService.class);

	private final PolicyRepository policyRepository;

	@Transactional
	public int processLapses() {
		LocalDate today = LocalDate.now();
		List<Policy> activePolicies = policyRepository.findByStatusAndNextPremiumDueDateIsNotNull(PolicyStatus.ACTIVE);

		int lapsedCount = 0;
		for (Policy policy : activePolicies) {
			if (policy.getPremiumFrequency() == null || policy.getNextPremiumDueDate() == null) {
				continue;
			}

			int graceDays = policy.getPremiumFrequency().getGracePeriodDays();
			LocalDate graceDeadline = policy.getNextPremiumDueDate().plusDays(graceDays);

			if (today.isAfter(graceDeadline)) {
				policy.setStatus(PolicyStatus.LAPSED);
				policy.setLapsedAt(today);
				policyRepository.save(policy);
				lapsedCount++;
				log.info("Policy lapsed: policyNumber={}, dueDate={}, graceDeadline={}", policy.getPolicyNumber(),
						policy.getNextPremiumDueDate(), graceDeadline);
			}
		}

		if (lapsedCount > 0) {
			log.info("Policy lapse check complete: {} polic{} lapsed today", lapsedCount,
					lapsedCount == 1 ? "y" : "ies");
		}
		return lapsedCount;
	}
}