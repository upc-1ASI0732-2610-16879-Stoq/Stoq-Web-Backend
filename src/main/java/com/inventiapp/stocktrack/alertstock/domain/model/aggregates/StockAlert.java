package com.inventiapp.stocktrack.alertstock.domain.model.aggregates;

import com.inventiapp.stocktrack.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persisted low-stock alert generated when available quantity falls below the product minimum.
 */
@Entity
@Table(name = "stock_alerts")
@Getter
@NoArgsConstructor
public class StockAlert extends AuditableAbstractAggregateRoot<StockAlert> {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer currentStock;

    @Column(nullable = false)
    private Integer minStock;

    public StockAlert(Long productId, Integer currentStock, Integer minStock) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("productId is required");
        }
        if (currentStock == null || currentStock < 0) {
            throw new IllegalArgumentException("currentStock must be non-negative");
        }
        if (minStock == null || minStock < 0) {
            throw new IllegalArgumentException("minStock must be non-negative");
        }
        this.productId = productId;
        this.currentStock = currentStock;
        this.minStock = minStock;
    }
}
