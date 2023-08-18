package com.example.stock.Service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.example.stock.domain.Stock;
import com.example.stock.facade.LettuceLockStockFacade;
import com.example.stock.facade.NamedLockStockFacade;
import com.example.stock.facade.OptimisticLockStockFacade;
import com.example.stock.repository.StockRepository;

@SpringBootTest
class StockServiceTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private PessimisticLockStockService pessimisticLockStockService;

	@Autowired
	private OptimisticLockStockFacade optimisticLockStockFacade;

	@Autowired
	private NamedLockStockFacade namedLockStockFacade;

	@Autowired
	private LettuceLockStockFacade lettuceLockStockFacade;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	public void before() {
		stockRepository.saveAndFlush(new Stock(1L, 100L));
	}

	@AfterEach
	public void after() {
		stockRepository.deleteAll();
	}

	@Test
	@DisplayName("요청이 한개만 들어오기 때문에 성공하는 테스트 코드")
	public void 재고감소() {
		// given

		// when
		stockService.decrease(1L, 1L);

		//then
		// 100 - 1 = 99
		Stock stock = stockRepository.findById(1L).orElseThrow();
		assertThat(stock.getQuantity()).isEqualTo(99);
	}

	@Test
	/*
	 - 해당 어노테이션을 붙이면 스프링의 테스트 컨텍스트 프레임워크에게 해당 클래스의 테스트에서
		애플리케이션 컨텍스트의 상태를 변경한다는 것을 알려준다.
     - 같은 Context를 사용하는 테스트간의 격리를 위해 사용
	 - 테스트 케이스가 실행된 후에 컨텍스트가 재로딩되어 다른 테스트 케이스에 영향을 주지 않습니다.
	 */
	@DirtiesContext
	@DisplayName("Race Condition이 발생")
	public void 동시에_100개의_요청_Race_Condition_발생() throws InterruptedException {
		int threadCount = 100;

		// ExecutorService: 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와줌
		ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드 풀의 개수 지정

		// 100개의 요청이 끝날때까지 기다려야하므로 CountDownLatch 활용
		// CountDownLatch: 다른 쓰레드에서 수행중인 작업이 완료될때 까지 대기할 수 있도록 도와주는 클래스
		CountDownLatch latch = new CountDownLatch(threadCount); // Latch 할 개수 지정

		for (int i = 0; i < threadCount; i++) { // 100개의 요청
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				} finally {
					latch.countDown(); // countDown(): Latch 의 카운터가 1개씩 감소
				}
			});
		}
		latch.await(); // await(): Latch 의 카운터가 0이 될 때까지 기다림

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// 예상대로 라면 100 - (1 * 100) = 0 이지만 RaceCondition 발생으로 인해 예상과 다른 결과가 나옴
		assertThat(stock.getQuantity()).isEqualTo(0);
	}

	@Test
	@DirtiesContext
	@DisplayName("synchronize 키워드 활용")
	public void 동시에_100개의_요청_synchronized() throws InterruptedException {
		// given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// then
		assertThat(stock.getQuantity()).isEqualTo(0);
	}

	@Test
	@DirtiesContext
	@DisplayName("Pessimistic Lock(비관적 락) 활용")
	public void 동시에_100개의_요청_pessimistic_lock() throws InterruptedException {
		// given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					pessimisticLockStockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// then
		assertThat(stock.getQuantity()).isEqualTo(0);
	}

	@Test
	@DirtiesContext
	@DisplayName("Optimistic Lock(낙관적 락) 활용")
	public void 동시에_100개의_요청_optimistic_lock() throws InterruptedException {
		// given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					optimisticLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// then
		assertThat(stock.getQuantity()).isEqualTo(0);
	}

	@Test
	@DirtiesContext
	@DisplayName("Named Lock(네임드 락) 활용")
	public void 동시에_100개의_요청_named_lock() throws InterruptedException {
		// given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					namedLockStockFacade.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// then
		assertThat(stock.getQuantity()).isEqualTo(0);
	}

	@Test
	@DirtiesContext
	@DisplayName("Lettuce 활용")
	public void 동시에_100개의_요청_lettuce() throws InterruptedException {
		// given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					lettuceLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// then
		assertThat(stock.getQuantity()).isEqualTo(0);
	}
}
