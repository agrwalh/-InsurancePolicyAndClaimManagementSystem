package com.harshit.monocept.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendOtp(String toEmail, String otp) {
		if (!StringUtils.hasText(fromEmail)) {
			throw new IllegalStateException(
					"Email service not configured. Please set spring.mail.username in application.properties.");
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail.trim());
			message.setTo(toEmail);
			message.setSubject("Insurance Account - Email Verification OTP");
			message.setText("Dear User,\n\n" + "Thank you for registering with Insurance Policy Management System.\n\n"
					+ "Your Email Verification OTP is: " + otp + "\n\n"
					+ "This OTP is valid for 5 minutes. Do not share it with anyone.\n\n" + "Regards,\n"
					+ "Insurance Policy Management Team");
			mailSender.send(message);
			log.info("Email OTP sent successfully to: {}", toEmail);

		} catch (MailException ex) {
			Throwable rootCause = ex;
			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}
			log.error("Failed to send email OTP to {}: {}", toEmail, rootCause.getMessage());
			throw new IllegalStateException("Unable to send email OTP. Root cause: "
					+ rootCause.getClass().getSimpleName() + " - " + rootCause.getMessage(), ex);
		}
	}
}