package com.harshit.monocept.service;

import java.security.SecureRandom;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harshit.monocept.entity.OtpVerification;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.OtpChannel;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.repository.OtpVerificationRepository;
import com.harshit.monocept.util.PhoneNumberUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

	private final OtpVerificationRepository otpRepository;
	private final EmailService emailService;
	private final PhoneOtpService phoneOtpService;
	private final PhoneNumberUtil phoneNumberUtil;
	private final SecureRandom secureRandom = new SecureRandom();

	@Value("${app.otp.expiry-minutes:5}")
	private long expiryMinutes;

	@Value("${app.otp.max-attempts:5}")
	private int maxAttempts;

	@Value("${app.otp.resend-cooldown-seconds:30}")
	private long resendCooldownSeconds;

	@Transactional
	public void createAndSendOtp(User user, OtpChannel channel) {
		enforceResendCooldown(user);

		if (channel == OtpChannel.EMAIL) {
			String emailOtp = generateSixDigitOtp();

			OtpVerification record = OtpVerification.builder().user(user).channel(OtpChannel.EMAIL).otpCode(emailOtp)
					.expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes)).used(false).attemptCount(0).build();
			otpRepository.save(record);

			emailService.sendOtp(user.getEmail(), emailOtp);
			log.info("Email OTP created and sent for user: {}", user.getEmail());

		} else {
			String e164 = phoneNumberUtil.toE164(user.getMobileNumber());

			OtpVerification record = OtpVerification.builder().user(user).channel(OtpChannel.PHONE).otpCode(null)
					.expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes)).used(false).attemptCount(0).build();
			otpRepository.save(record);

			phoneOtpService.sendOtp(e164);
			log.info("Phone OTP dispatched via Twilio for user: {}", user.getEmail());
		}
	}

	@Transactional
	public void verifyOtp(User user, OtpChannel channel, String submittedOtp) {
		OtpVerification latestOtp = otpRepository.findTopByUserAndChannelAndUsedFalseOrderByCreatedAtDesc(user, channel)
				.orElseThrow(() -> new BusinessRuleException(
						"No active OTP found for this channel. Please use Resend OTP."));

		if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessRuleException("OTP has expired. Please use the Resend OTP option.");
		}

		if (latestOtp.getAttemptCount() >= maxAttempts) {
			throw new BusinessRuleException(
					"Too many incorrect attempts for this OTP. Please request a new one via Resend OTP.");
		}

		boolean valid;
		if (channel == OtpChannel.EMAIL) {
			valid = latestOtp.getOtpCode() != null && latestOtp.getOtpCode().equals(submittedOtp);
		} else {
			String e164 = phoneNumberUtil.toE164(user.getMobileNumber());
			valid = phoneOtpService.checkOtp(e164, submittedOtp);
		}

		if (!valid) {
			latestOtp.setAttemptCount(latestOtp.getAttemptCount() + 1);
			otpRepository.save(latestOtp);
			int remaining = maxAttempts - latestOtp.getAttemptCount();
			throw new BusinessRuleException(
					"Invalid OTP. Please check and try again. Attempts remaining: " + Math.max(remaining, 0));
		}

		latestOtp.setUsed(true);
		otpRepository.save(latestOtp);
		log.info("{} OTP verified successfully for user: {}", channel, user.getEmail());
	}

	private void enforceResendCooldown(User user) {
		otpRepository.findTopByUserOrderByCreatedAtDesc(user).ifPresent(lastOtp -> {
			long secondsSinceLast = java.time.Duration.between(lastOtp.getCreatedAt(), LocalDateTime.now())
					.getSeconds();
			if (secondsSinceLast < resendCooldownSeconds) {
				throw new BusinessRuleException("Please wait " + (resendCooldownSeconds - secondsSinceLast)
						+ " more second(s) before requesting another OTP.");
			}
		});
	}

	private String generateSixDigitOtp() {
		int number = secureRandom.nextInt(900000) + 100000;
		return String.valueOf(number);
	}
}