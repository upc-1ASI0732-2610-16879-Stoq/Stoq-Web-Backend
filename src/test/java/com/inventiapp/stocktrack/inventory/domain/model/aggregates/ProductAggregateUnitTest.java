package com.inventiapp.stocktrack.inventory.domain.model.aggregates;

import com.inventiapp.stocktrack.inventory.domain.model.commands.CreateProductCommand;
import com.inventiapp.stocktrack.inventory.domain.model.commands.UpdateProductCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductAggregateUnitTest {

    @Test
    // US01 - Registrar producto nuevo: creación válida.
    void shouldCreateProductWithValidData() {
        CreateProductCommand command = new CreateProductCommand(
                "  Arroz Extra  ",
                "  Bolsa de 5kg  ",
                "1",
                "10",
                8,
                12.5,
                true
        );

        Product product = new Product(command);

        assertNotNull(product);
        assertEquals("Arroz Extra", product.getName());
        assertEquals("Bolsa de 5kg", product.getDescription());
        assertEquals("1", product.getCategoryId());
        assertEquals("10", product.getProviderId());
        assertEquals(8, product.getMinStock());
        assertEquals(12.5, product.getUnitPrice());
        assertEquals(true, product.getIsActive());
    }

    @Test
    // US01 - Registrar producto nuevo: datos vacíos/comando nulo.
    void shouldRejectNullCreateCommand() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Product(null)
        );

        assertEquals("CreateProductCommand is required", ex.getMessage());
    }

    @Test
    // US01 - Registrar producto nuevo: nombre vacío.
    void shouldRejectBlankNameInCreateCommand() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CreateProductCommand("   ", "desc", "1", "10", 5, 9.9, true)
        );

        assertEquals("name cannot be null or blank", ex.getMessage());
    }

    @Test
    // US01 + US19 - stock inicial/minStock válido en 0.
    void shouldAllowZeroMinStockAsInitialStockThreshold() {
        CreateProductCommand command = new CreateProductCommand(
                "Aceite",
                "Botella 1L",
                "2",
                "11",
                0,
                7.2,
                true
        );

        Product product = new Product(command);

        assertEquals(0, product.getMinStock());
    }

    @Test
    // US19 - Configurar stock mínimo: guardar nuevo límite.
    void shouldUpdateMinStockWhenCommandIsValid() {
        Product product = new Product(new CreateProductCommand(
                "Fideos",
                "Paquete",
                "3",
                "12",
                4,
                3.5,
                true
        ));

        UpdateProductCommand update = new UpdateProductCommand(
                1L,
                "Fideos Premium",
                "Paquete grande",
                "3",
                "12",
                9,
                4.0,
                true
        );

        product.updateProduct(update);

        assertEquals("Fideos Premium", product.getName());
        assertEquals(9, product.getMinStock());
        assertEquals(4.0, product.getUnitPrice());
    }

    @Test
    // US19 - Configurar stock mínimo: validar negativos.
    void shouldRejectNegativeMinStockOnUpdateCommand() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new UpdateProductCommand(1L, "Pan", "desc", "1", "2", -1, 1.5, true)
        );

        assertEquals("minStock cannot be null or negative", ex.getMessage());
    }
}
