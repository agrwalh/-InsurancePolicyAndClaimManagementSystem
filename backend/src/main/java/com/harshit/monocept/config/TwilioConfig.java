package com.harshit.monocept.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.twilio.Twilio;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TwilioConfig {

	@Value("${twilio.enabled:false}")
	private boolean twilioEnabled;

	@Value("${twilio.account-sid:}")
	private String accountSid;

	@Value("${twilio.auth-token:}")
	private String authToken;

	@EventListener(ApplicationReadyEvent.class)
	public void initTwilio() {
		if (!twilioEnabled) {
			log.warn("Twilio is DISABLED (twilio.enabled=false). Phone OTPs will be logged locally "
					+ "instead of sent via SMS. Set TWILIO_ENABLED=true with valid credentials to go live.");
			return;
		}

		if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
			log.error("twilio.enabled=true but TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN are missing. "
					+ "Phone OTP sending will fail until these are configured.");
			return;
		}

		Twilio.init(accountSid, authToken);
		log.info("Twilio SDK initialized successfully.");
	}
}