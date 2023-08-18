package com.example.stock.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.Service.StockService;
import com.example.stock.repository.LockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NamedLockStockFacade {

	private final StockService stockService;

	private final LockRepository lockRepository;

	@Transactional
	public void decrease(Long id, Long quantity) {
		try {
			lockRepository.getLock(id.toString()); // 락 획득
			stockService.decrease(id, quantity);
		} finally {
			lockRepository.releaseLock(id.toString()); // 락 해제
		}
	}
}
