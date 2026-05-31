package com.inventiapp.stocktrack.alertstock.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventiapp.stocktrack.alertstock.infrastructure.persistence.jpa.repositories.StockAlertRepository;
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
 * Integration tests for automatic low-stock alerts persisted after inventory movements.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser(username = "testuser", roles = {"ADMIN"})
@DisplayName("Stock alert integration - low stock persistence")
class StockAlertIntegrationTest {

    private static final String CATEGORY_ENDPOINT = "/api/v1/categories";
    private static final String PROVIDER_ENDPOINT = "/api/v1/providers";
    private static final String PRODUCT_ENDPOINT = "/api/v1/products";
    private static final String BATCH_ENDPOINT = "/api/v1/batches";
    private static final String SALES_ENDPOINT = "/api/v1/sales";

    private static final String CATEGORY_NAME = "Lácteos";
    private static final String CATEGORY_DESCRIPTION = "Productos lácteos refrigerados";
    private static final String PROVIDER_FIRST_NAME = "Carlos";
    private static final String PROVIDER_LAST_NAME = "Rivas";
    private static final String PROVIDER_PHONE = "912345678";
    private static final String PROVIDER_EMAIL = "carlos.rivas@lacteos.pe";
    private static final String PROVIDER_TAX_ID = "20987654321";

    private static final String PRODUCT_NAME = "Leche entera 1L";
    private static final String PRODUCT_DESCRIPTION = "Leche UHT entera";
    private static final double PRODUCT_UNIT_PRICE = 4.20;

    private static final int MIN_STOCK_TRIGGERING_ALERT = 10;
    private static final int INITIAL_STOCK_ABOVE_MINIMUM = 18;
    private static final int OUTBOUND_QUANTITY_CAUSING_ALERT = 9;
    private static final long EXPECTED_ALERT_COUNT_AFTER_LOW_STOCK = 1L;

    private static final int MIN_STOCK_WITHOUT_ALERT = 5;
    private static final int INITIAL_STOCK_COMFORTABLE = 20;
    private static final int OUTBOUND_QUANTITY_STILL_ABOVE_MINIMUM = 4;
    private static final long EXPECTED_ALERT_COUNT_WHEN_STOCK_REMAINS_OK = 0L;

    private static final long STAFF_USER_ID = 1L;
    private static final int BATCH_EXPIRATION_OFFSET_DAYS = 60;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockAlertRepository stockAlertRepository;

    @BeforeEach
    void setUp() {
        stockAlertRepository.deleteAll();
    }

    @Test
    @DisplayName("US05 - Salida que deja stock bajo el mínimo genera alerta persistida")
    void should_PersistStockAlert_when_OutboundLeavesStockBelowMinimum() throws Exception {
        // Arrange
        Long productId = createTrackedProduct(MIN_STOCK_TRIGGERING_ALERT, INITIAL_STOCK_ABOVE_MINIMUM);

        // Act
        registerStockOutbound(productId, OUTBOUND_QUANTITY_CAUSING_ALERT);

        // Assert
        assertEquals(EXPECTED_ALERT_COUNT_AFTER_LOW_STOCK, stockAlertRepository.countByProductId(productId));
    }

    @Test
    @DisplayName("US05 - No se genera alerta cuando el stock sigue en o sobre el mínimo")
    void should_NotPersistStockAlert_when_RemainingStockIsAtOrAboveMinimum() throws Exception {
        // Arrange
        Long productId = createTrackedProduct(MIN_STOCK_WITHOUT_ALERT, INITIAL_STOCK_COMFORTABLE);

        // Act
        registerStockOutbound(productId, OUTBOUND_QUANTITY_STILL_ABOVE_MINIMUM);

        // Assert
        assertEquals(EXPECTED_ALERT_COUNT_WHEN_STOCK_REMAINS_OK, stockAlertRepository.countByProductId(productId));
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private Long createTrackedProduct(int minStock, int initialBatchQuantity) throws Exception {
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
                        minStock,
                        PRODUCT_UNIT_PRICE,
                        true)));

        Date receptionDate = new Date();
        postAndExpectCreated(
                BATCH_ENDPOINT,
                new CreateBatchResource(
                        productId,
                        initialBatchQuantity,
                        addDays(BATCH_EXPIRATION_OFFSET_DAYS),
                        receptionDate));

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

    private Date addDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }
}