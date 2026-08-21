package com.harshit.monocept.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.ForgotPasswordRequest;
import com.harshit.monocept.dto.request.LoginRequest;
import com.harshit.monocept.dto.request.RegisterRequest;
import com.harshit.monocept.dto.request.ResendOtpRequest;
import com.harshit.monocept.dto.request.ResetPasswordRequest;
import com.harshit.monocept.dto.request.VerifyOtpRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.LoginResponse;
import com.harshit.monocept.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
		var user = authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Registration successful! OTP sent via " + req.getOtpChannel()
						+ ". Please verify to activate your account.", "User ID: " + user.getId()));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
		authService.verifyOtp(req);
		return ResponseEntity.ok(
				ApiResponse.success("Account verified successfully via " + req.getOtpChannel() + "! You can now login.",
						"Verified: " + req.getEmail()));
	}

	@PostMapping("/resend-otp")
	public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody ResendOtpRequest req) {
		authService.resendOtp(req);
		return ResponseEntity.ok(ApiResponse.success("New OTP sent via " + req.getOtpChannel() + ".",
				"OTP resent for: " + req.getEmail()));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(req)));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
		authService.forgotPassword(req);
		return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent via " + req.getOtpChannel(),
				"OTP sent for: " + req.getEmail()));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		authService.logout(authHeader);
		return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
		authService.resetPassword(req);
		return ResponseEntity
				.ok(ApiResponse.success("Password reset successful. You can now login with your new password.",
						"Password reset for: " + req.getEmail()));
	}
}