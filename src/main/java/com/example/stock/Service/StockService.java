package com.example.stock.Service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// @Transactional(readOnly = true)
public class StockService {

	private final StockRepository stockRepository;

	/*
	 	synchronized: 하나의 프로세스 안에서만 보장됨, 서버가 2대 이상일 경우
	 	데이터에 여러대에서 동시에 접근 할 수 있기 때문에 Race Condition 이 발생할 수 있다.
	 	실제 운영중인 서비스는 대부분 2대 이상의 서버를 사용하기 때문에 synchronized 는 거의 사용되지 않는다.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW) // 부모의 트랜잭션과 별도로 실행되어야 함
	public /*synchronized*/ void decrease(Long id, Long quantity) {
		// Stock 조회
		Stock stock = stockRepository.findById(id).orElseThrow(NoSuchElementException::new);

		// 재고 감소 시킨 뒤
		stock.decrease(quantity);

		// 갱신된 값을 저장
		stockRepository.saveAndFlush(stock); // saveAndFlush: 즉시 DB에 데이터를 반영
	}
}
