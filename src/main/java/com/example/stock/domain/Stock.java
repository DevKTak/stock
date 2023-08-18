package com.example.stock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;

	private Long quantity;

	@Version // javax.persistence.Version
	private Long version;

	public Stock(final Long productId, final Long quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public void decrease(final Long quantity) {
		if (this.quantity - quantity < 0) {
			throw new RuntimeException("재고가 부족합니다.");
		}
		this.quantity -= quantity;
	}
}
