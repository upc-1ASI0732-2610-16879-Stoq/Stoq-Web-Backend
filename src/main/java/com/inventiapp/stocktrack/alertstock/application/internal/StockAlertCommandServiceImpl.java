package com.inventiapp.stocktrack.alertstock.application.internal;

import com.inventiapp.stocktrack.alertstock.domain.model.aggregates.StockAlert;
import com.inventiapp.stocktrack.alertstock.infrastructure.persistence.jpa.repositories.StockAlertRepository;
import com.inventiapp.stocktrack.inventory.domain.model.aggregates.Batch;
import com.inventiapp.stocktrack.inventory.domain.model.queries.GetAllBatchesByProductIdQuery;
import com.inventiapp.stocktrack.inventory.domain.model.queries.GetProductByIdQuery;
import com.inventiapp.stocktrack.inventory.domain.services.BatchQueryService;
import com.inventiapp.stocktrack.inventory.domain.services.ProductQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockAlertCommandServiceImpl {

    private final StockAlertRepository stockAlertRepository;
    private final ProductQueryService productQueryService;
    private final BatchQueryService batchQueryService;

    public StockAlertCommandServiceImpl(StockAlertRepository stockAlertRepository,
                                        ProductQueryService productQueryService,
                                        BatchQueryService batchQueryService) {
        this.stockAlertRepository = stockAlertRepository;
        this.productQueryService = productQueryService;
        this.batchQueryService = batchQueryService;
    }

    @Transactional
    public void evaluateLowStockAfterMovement(Long productId) {
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        if (product.isEmpty()) {
            return;
        }

        int currentStock = batchQueryService.handle(new GetAllBatchesByProductIdQuery(productId)).stream()
                .mapToInt(Batch::getQuantity)
                .sum();

        int minStock = product.get().getMinStock();
        if (currentStock < minStock) {
            stockAlertRepository.save(new StockAlert(productId, currentStock, minStock));
        }
    }
}
