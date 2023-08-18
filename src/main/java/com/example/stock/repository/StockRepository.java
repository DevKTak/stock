package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.stock.domain.Stock;

import jakarta.persistence.LockModeType;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락
	@Query("select s from Stock s where s.id = :id")
	Stock findByIdWithPessimisticLock(@Param("id") Long id);

	@Lock(LockModeType.OPTIMISTIC) // 낙관적 락
	@Query("select s from Stock s where s.id = :id")
	Stock findByIdWithOptimisticLock(@Param("id") Long id);

}
