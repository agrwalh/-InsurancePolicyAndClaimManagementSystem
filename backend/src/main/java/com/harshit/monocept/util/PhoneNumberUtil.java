package com.harshit.monocept.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PhoneNumberUtil {

	@Value("${app.phone.default-country-code:+91}")
	private String defaultCountryCode;

	public String toE164(String rawNumber) {
		if (!StringUtils.hasText(rawNumber)) {
			throw new IllegalArgumentException("Phone number must not be empty");
		}

		String trimmed = rawNumber.trim();

		if (trimmed.startsWith("+")) {
			return trimmed;
		}

		String digitsOnly = trimmed.replaceAll("[^0-9]", "");
		return defaultCountryCode + digitsOnly;
	}
}