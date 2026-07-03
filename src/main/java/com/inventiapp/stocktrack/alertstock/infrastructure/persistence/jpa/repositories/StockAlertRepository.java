package com.inventiapp.stocktrack.alertstock.infrastructure.persistence.jpa.repositories;

import com.inventiapp.stocktrack.alertstock.domain.model.aggregates.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    long countByProductId(Long productId);
}
