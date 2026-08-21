package com.harshit.monocept.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.harshit.monocept.service.PolicyLapseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyLapseScheduler {

	private static final Logger log = LoggerFactory.getLogger(PolicyLapseScheduler.class);

	private final PolicyLapseService policyLapseService;

	@Scheduled(cron = "*/10 * * * * *")
	public void checkForLapsedPolicies() {
		log.info("Running daily policy lapse check...");
		int count = policyLapseService.processLapses();
		log.info("Daily policy lapse check finished. {} polic{} lapsed.", count, count == 1 ? "y" : "ies");
	}
}