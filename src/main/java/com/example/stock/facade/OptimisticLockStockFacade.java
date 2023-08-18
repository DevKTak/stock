package com.example.stock.facade;

import org.springframework.stereotype.Component;

import com.example.stock.Service.OptimisticLockStockService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

	private final OptimisticLockStockService optimisticLockStockService;

	public void decrease(Long id, Long quantity) throws InterruptedException {
		while (true) {
			try {
				optimisticLockStockService.decrease(id, quantity);

				break; // 정상적으로 수량 감소가 됐다면 while 문 탈출
			} catch (Exception e) {
				Thread.sleep(50); // 수량 감소에 실패하게 된다면 50 millis 대기 후 재시도
			}
		}

	}
}
