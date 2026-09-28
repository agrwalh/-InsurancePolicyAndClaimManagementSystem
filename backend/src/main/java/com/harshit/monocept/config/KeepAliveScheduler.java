package com.harshit.monocept.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KeepAliveScheduler {

	private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	@Scheduled(fixedRate = 4 * 60 * 1000) // every 4 minutes
	public void warmConnections() {
		try {
			userRepository.count();
		} catch (Exception e) {
			log.warn("Keep-alive DB ping failed: {}", e.getMessage());
		}

		try {
			redisTemplate.opsForValue().set("keepalive:ping", "1");
		} catch (Exception e) {
			log.warn("Keep-alive Redis ping failed: {}", e.getMessage());
		}
	}
}
