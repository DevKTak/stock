package com.example.stock.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.Service.StockService;
import com.example.stock.repository.RedisLockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

	private final RedisLockRepository redisLockRepository;
	private final StockService stockService;

	@Transactional
	public void decrease(Long id, Long quantity) throws InterruptedException {
		while (!redisLockRepository.lock(id)) {
			// 락 획득에 실패하면 sleep을 통해 텀을 주고 락 획득을 재시도(레디스의 부하를 줄여주기 위함)
			Thread.sleep(100);
		}

		try {
			stockService.decrease(id, quantity);
		} finally {
			redisLockRepository.unlock(id);
		}
	}
}
