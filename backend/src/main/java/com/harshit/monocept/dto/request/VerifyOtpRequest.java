package com.harshit.monocept.dto.request;

import com.harshit.monocept.enums.OtpChannel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotNull(message = "OTP channel is required (EMAIL or PHONE)")
	private OtpChannel otpChannel;

	@NotBlank(message = "OTP is required")
	private String otp;
}