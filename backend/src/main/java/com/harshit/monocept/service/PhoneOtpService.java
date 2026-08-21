package com.harshit.monocept.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.harshit.monocept.exception.BusinessRuleException;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhoneOtpService {

	@Value("${twilio.enabled:false}")
	private boolean twilioEnabled;

	@Value("${twilio.verify-service-sid:}")
	private String verifyServiceSid;

	public void sendOtp(String e164PhoneNumber) {
		if (!twilioEnabled) {
			log.warn("[DEV MODE] Twilio disabled - pretending to send phone OTP to {}. No real SMS sent.",
					maskPhone(e164PhoneNumber));
			return;
		}

		assertConfigured();

		try {
			Verification verification = Verification.creator(verifyServiceSid, e164PhoneNumber, "sms").create();
			log.info("Twilio phone OTP dispatched to {} (status: {})", maskPhone(e164PhoneNumber),
					verification.getStatus());
		} catch (ApiException ex) {
			log.error("Twilio failed to send phone OTP to {}: {}", maskPhone(e164PhoneNumber), ex.getMessage());
			throw new BusinessRuleException(
					"Unable to send phone OTP right now. Please try again or verify via email instead.");
		}
	}

	public boolean checkOtp(String e164PhoneNumber, String submittedCode) {
		if (!twilioEnabled) {
			boolean matches = "123456".equals(submittedCode);
			log.warn("[DEV MODE] Twilio disabled - validating phone OTP for {} against dev code 123456: {}",
					maskPhone(e164PhoneNumber), matches);
			return matches;
		}

		assertConfigured();

		try {
			VerificationCheck check = VerificationCheck.creator(verifyServiceSid).setTo(e164PhoneNumber)
					.setCode(submittedCode).create();
			return "approved".equalsIgnoreCase(check.getStatus());
		} catch (ApiException ex) {
			log.warn("Twilio OTP check failed for {}: {}", maskPhone(e164PhoneNumber), ex.getMessage());
			return false;
		}
	}

	private void assertConfigured() {
		if (!StringUtils.hasText(verifyServiceSid)) {
			throw new IllegalStateException(
					"Twilio Verify Service SID is not configured. Set twilio.verify-service-sid.");
		}
	}

	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 4) {
			return "****";
		}
		return phone.substring(0, phone.length() - 4).replaceAll(".", "*") + phone.substring(phone.length() - 4);
	}
}