package com.harshit.monocept.dto.request;

import com.harshit.monocept.enums.OtpChannel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotNull(message = "OTP channel is required")
	private OtpChannel otpChannel;

	@NotBlank(message = "OTP is required")
	private String otp;

	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String newPassword;
}