package com.harshit.monocept.config;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.OtpChannel;
import com.harshit.monocept.enums.Role;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		createAdminIfNotExists();
	}

	private void createAdminIfNotExists() {

		if (userRepository.existsByEmail("admin@gmail.com")) {
			log.info("Admin already exists — skipping creation");
			return;
		}

			User admin = User.builder().fullName("Admin User").email("admin@gmail.com")
					.password(passwordEncoder.encode("password123")).mobileNumber("9999999999").role(Role.ADMIN)
					.preferredOtpChannel(OtpChannel.EMAIL).isActive(true).isVerified(true).emailVerified(true)
					.phoneVerified(true).build();

		userRepository.save(admin);
		log.info("Admin created successfully: admin@gmail.com");
	}
}