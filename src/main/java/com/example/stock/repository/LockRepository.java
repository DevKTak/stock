package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.stock.domain.Stock;

/**
 * 네임드 락
 */
@Repository
public interface LockRepository extends JpaRepository<Stock, Long> {
	@Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
	void getLock(String key); // 락 획득

	@Query(value = "select release_lock(:key)", nativeQuery = true)
	void releaseLock(String key); // 락 해제
}
