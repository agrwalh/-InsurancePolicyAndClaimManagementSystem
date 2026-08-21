package com.harshit.monocept.security;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private static final String PREFIX = "blacklist:token:";

	private final RedisTemplate<String, Object> redisTemplate;

	public void blacklistToken(String token, long remainingExpiryMillis) {
		if (remainingExpiryMillis <= 0) {
			return; // already expired, no need to store
		}
		redisTemplate.opsForValue().set(PREFIX + token, "true", remainingExpiryMillis, TimeUnit.MILLISECONDS);
	}

	public boolean isBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
	}
}