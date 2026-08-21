package com.harshit.monocept.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.OtpVerification;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.OtpChannel;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

	Optional<OtpVerification> findTopByUserAndChannelAndUsedFalseOrderByCreatedAtDesc(User user, OtpChannel channel);

	Optional<OtpVerification> findTopByUserOrderByCreatedAtDesc(User user);
}