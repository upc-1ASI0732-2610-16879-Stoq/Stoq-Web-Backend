package com.inventiapp.stocktrack.inventory.domain.model.aggregates;

import com.inventiapp.stocktrack.inventory.domain.model.commands.CreateBatchCommand;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchAggregateUnitTest {

    @Test
    // US03 - Registrar salida de producto: estado base para operar stock.
    void shouldCreateBatchWithValidData() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);

        Batch batch = new Batch(new CreateBatchCommand(1L, 20, expiration, now));

        assertEquals(1L, batch.getProductId());
        assertEquals(20, batch.getQuantity());
    }

    @Test
    // US03 - Registrar salida de producto: descuento correcto del stock.
    void shouldDecreaseStockForProductOutput() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);
        Batch batch = new Batch(new CreateBatchCommand(5L, 15, expiration, now));

        batch.reduceQuantity(6);

        assertEquals(9, batch.getQuantity());
    }

    @Test
    // US03 - Registrar salida de producto: impedir stock negativo.
    void shouldPreventNegativeStockWhenReducing() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);
        Batch batch = new Batch(new CreateBatchCommand(5L, 4, expiration, now));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> batch.reduceQuantity(5)
        );

        assertEquals("Insufficient quantity in batch", ex.getMessage());
    }

    @Test
    // US03 - Registrar salida de producto: validar cantidad inválida (negativa).
    void shouldValidateOutputQuantityCannotBeNegative() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);
        Batch batch = new Batch(new CreateBatchCommand(5L, 10, expiration, now));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> batch.reduceQuantity(-1)
        );

        assertEquals("Reduction amount cannot be negative", ex.getMessage());
    }

    @Test
    // US14 - Registrar devolución: aumentar stock.
    void shouldIncreaseStockWhenRegisteringReturn() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);
        Batch batch = new Batch(new CreateBatchCommand(7L, 10, expiration, now));

        int returnedQuantity = 3;
        batch.setQuantity(batch.getQuantity() + returnedQuantity);

        assertEquals(13, batch.getQuantity());
    }

    @Test
    // US14 - Registrar devolución: validar cantidades inválidas.
    void shouldRejectNegativeQuantityWhenSettingStock() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);
        Batch batch = new Batch(new CreateBatchCommand(9L, 10, expiration, now));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> batch.setQuantity(-5)
        );

        assertEquals("Quantity cannot be negative", ex.getMessage());
    }

    @Test
    // US03/US14 - consistencia de stock: no permitir stock inicial negativo.
    void shouldRejectNegativeInitialStock() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 86_400_000L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CreateBatchCommand(2L, -1, expiration, now)
        );

        assertEquals("quantity cannot be null or negative", ex.getMessage());
    }
}
