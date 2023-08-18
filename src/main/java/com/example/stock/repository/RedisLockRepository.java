package com.example.stock.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Component
@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

	private RedisTemplate<String, String> redisTemplate;

	public Boolean lock(Long key) { // 락 획득
		return redisTemplate
			.opsForValue()
			.setIfAbsent(generateKey(key), "lock", Duration.ofMillis(9_000));
	}

	public Boolean unlock(Long key) { // 락 해제
		return redisTemplate.delete(generateKey(key));
	}

	private String generateKey(Long key) {
		return key.toString();
	}
}
