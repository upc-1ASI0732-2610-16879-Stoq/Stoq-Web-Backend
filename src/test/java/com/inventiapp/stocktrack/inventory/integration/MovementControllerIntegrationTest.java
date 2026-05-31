package com.inventiapp.stocktrack.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventiapp.stocktrack.inventory.infrastructure.persistence.jpa.repositories.BatchRepository;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateBatchResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateCategoryResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateProductResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateProviderResource;
import com.inventiapp.stocktrack.sales.interfaces.rest.resources.CreateSaleResource;
import com.inventiapp.stocktrack.sales.interfaces.rest.resources.ProductSaleItemResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for stock movements (inventory outbound via sales, inbound via batch replenishment).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser(username = "testuser", roles = {"ADMIN"})
@DisplayName("Movement integration - stock inbound and outbound")
class MovementControllerIntegrationTest {

    private static final String CATEGORY_ENDPOINT = "/api/v1/categories";
    private static final String PROVIDER_ENDPOINT = "/api/v1/providers";
    private static final String PRODUCT_ENDPOINT = "/api/v1/products";
    private static final String BATCH_ENDPOINT = "/api/v1/batches";
    private static final String SALES_ENDPOINT = "/api/v1/sales";

    private static final String CATEGORY_NAME = "Bebidas";
    private static final String CATEGORY_DESCRIPTION = "Bebidas y refrescos para retail";
    private static final String PROVIDER_FIRST_NAME = "Laura";
    private static final String PROVIDER_LAST_NAME = "Mendoza";
    private static final String PROVIDER_PHONE = "987654321";
    private static final String PROVIDER_EMAIL = "laura.mendoza@distribuidora.pe";
    private static final String PROVIDER_TAX_ID = "20123456789";

    private static final String PRODUCT_NAME = "Agua mineral 625ml";
    private static final String PRODUCT_DESCRIPTION = "Botella de agua mineral sin gas";
    private static final int PRODUCT_MIN_STOCK = 5;
    private static final double PRODUCT_UNIT_PRICE = 2.50;

    private static final int INITIAL_BATCH_QUANTITY = 40;
    private static final int OUTBOUND_SALE_QUANTITY = 12;
    private static final int EXPECTED_STOCK_AFTER_OUTBOUND = INITIAL_BATCH_QUANTITY - OUTBOUND_SALE_QUANTITY;

    private static final int REPLENISHMENT_BATCH_QUANTITY = 25;
    private static final int EXPECTED_STOCK_AFTER_INBOUND = INITIAL_BATCH_QUANTITY + REPLENISHMENT_BATCH_QUANTITY;

    private static final int AVAILABLE_STOCK_FOR_REJECTION = 6;
    private static final int EXCESS_OUTBOUND_QUANTITY = 10;

    private static final long STAFF_USER_ID = 1L;
    private static final int BATCH_EXPIRATION_OFFSET_DAYS = 90;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatchRepository batchRepository;

    @BeforeEach
    void setUp() {
        batchRepository.deleteAll();
    }

    @Test
    @DisplayName("US03 - Salida válida descuenta inventario en base de datos")
    void should_DecreaseInventoryStock_when_ValidStockOutboundIsRegistered() throws Exception {
        // Arrange
        Long productId = createProductWithInitialStock(INITIAL_BATCH_QUANTITY);

        // Act
        registerStockOutbound(productId, OUTBOUND_SALE_QUANTITY);

        // Assert
        assertEquals(EXPECTED_STOCK_AFTER_OUTBOUND, sumStockForProduct(productId));
    }

    @Test
    @DisplayName("US14 - Entrada de reposición incrementa inventario en base de datos")
    void should_IncreaseInventoryStock_when_ValidStockInboundIsRegistered() throws Exception {
        // Arrange
        Long productId = createProductWithInitialStock(INITIAL_BATCH_QUANTITY);

        // Act
        registerStockInbound(productId, REPLENISHMENT_BATCH_QUANTITY);

        // Assert
        assertEquals(EXPECTED_STOCK_AFTER_INBOUND, sumStockForProduct(productId));
    }

    @Test
    @DisplayName("US03 - Salida con cantidad mayor al stock disponible es rechazada")
    void should_RejectOutbound_when_QuantityExceedsAvailableStock() throws Exception {
        // Arrange
        Long productId = createProductWithInitialStock(AVAILABLE_STOCK_FOR_REJECTION);

        // Act
        MvcResult result = mockMvc.perform(post(SALES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateSaleResource(
                                        STAFF_USER_ID,
                                        List.of(new ProductSaleItemResource(productId, EXCESS_OUTBOUND_QUANTITY)),
                                        null))))
                .andReturn();

        // Assert
        assertEquals(400, result.getResponse().getStatus());
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private Long createProductWithInitialStock(int batchQuantity) throws Exception {
        Long categoryId = extractId(postAndExpectCreated(
                CATEGORY_ENDPOINT,
                new CreateCategoryResource(CATEGORY_NAME, CATEGORY_DESCRIPTION)));

        Long providerId = extractId(postAndExpectCreated(
                PROVIDER_ENDPOINT,
                new CreateProviderResource(
                        PROVIDER_FIRST_NAME,
                        PROVIDER_LAST_NAME,
                        PROVIDER_PHONE,
                        PROVIDER_EMAIL,
                        PROVIDER_TAX_ID)));

        Long productId = extractId(postAndExpectCreated(
                PRODUCT_ENDPOINT,
                new CreateProductResource(
                        PRODUCT_NAME,
                        PRODUCT_DESCRIPTION,
                        String.valueOf(categoryId),
                        String.valueOf(providerId),
                        PRODUCT_MIN_STOCK,
                        PRODUCT_UNIT_PRICE,
                        true)));

        Date receptionDate = new Date();
        registerStockInbound(productId, batchQuantity, receptionDate);
        return productId;
    }

    private void registerStockOutbound(Long productId, int quantity) throws Exception {
        mockMvc.perform(post(SALES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateSaleResource(
                                        STAFF_USER_ID,
                                        List.of(new ProductSaleItemResource(productId, quantity)),
                                        null))))
                .andExpect(status().isCreated());
    }

    private void registerStockInbound(Long productId, int quantity) throws Exception {
        registerStockInbound(productId, quantity, new Date());
    }

    private void registerStockInbound(Long productId, int quantity, Date receptionDate) throws Exception {
        postAndExpectCreated(
                BATCH_ENDPOINT,
                new CreateBatchResource(productId, quantity, addDays(BATCH_EXPIRATION_OFFSET_DAYS), receptionDate));
    }

    private String postAndExpectCreated(String endpoint, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private Long extractId(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private int sumStockForProduct(Long productId) {
        return batchRepository.findByProductIdOrderByExpirationDateAsc(productId).stream()
                .mapToInt(batch -> batch.getQuantity())
                .sum();
    }

    private Date addDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }
}