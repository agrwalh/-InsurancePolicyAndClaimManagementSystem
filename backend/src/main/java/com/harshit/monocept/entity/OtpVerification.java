package com.harshit.monocept.entity;

import java.time.LocalDateTime;

import com.harshit.monocept.enums.OtpChannel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_verifications", indexes = {
		@Index(name = "idx_otp_user_channel_used_created", columnList = "user_id, channel, used, createdAt"),
		@Index(name = "idx_otp_user_created", columnList = "user_id, createdAt") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OtpChannel channel;

	@Column(name = "otp_code")
	private String otpCode;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Builder.Default
	private boolean used = false;

	@Builder.Default
	@Column(nullable = false)
	private int attemptCount = 0;

	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		createdAt = LocalDateTime.now();
	}
}