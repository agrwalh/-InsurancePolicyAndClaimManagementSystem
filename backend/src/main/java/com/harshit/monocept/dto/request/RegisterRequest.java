package com.harshit.monocept.dto.request;

import com.harshit.monocept.enums.OtpChannel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
	@NotBlank(message = "Full name is required")
	private String fullName;

	@NotBlank
	@Email(message = "Invalid email")
	private String email;

	@NotBlank
	@Size(min = 8, message = "Min 8 characters")
	private String password;

	@NotBlank(message = "Mobile number is required")
	@Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
	private String mobileNumber;

	@NotNull(message = "Please choose a verification method: EMAIL or PHONE")
	private OtpChannel otpChannel;
}