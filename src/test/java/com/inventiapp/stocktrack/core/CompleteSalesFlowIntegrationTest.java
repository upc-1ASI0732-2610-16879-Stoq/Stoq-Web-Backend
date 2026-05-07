package com.inventiapp.stocktrack.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateBatchResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateCategoryResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateProductResource;
import com.inventiapp.stocktrack.inventory.interfaces.rest.resources.CreateProviderResource;
import com.inventiapp.stocktrack.sales.interfaces.rest.resources.CreateSaleResource;
import com.inventiapp.stocktrack.sales.interfaces.rest.resources.ProductSaleItemResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Core System Test - Complete Sales Flow with FIFO Stock Reduction
 *
 * Validates: Product -> Batches (3 diferentes) -> Sale -> FIFO Stock Reduction
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Flujo Completo de Venta con Reduccion FIFO")
public class CompleteSalesFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private Long categoryId;
    private Long providerId;
    private Long productId;

    private Long extractIdFromResponse(String response) {
        try {
            return objectMapper.readTree(response).get("id").asLong();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer ID de la respuesta", e);
        }
    }


    private Date addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }


    private String postRequest(String endpoint, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return result.getResponse().getContentAsString();
    }


    private void getAndValidate(String endpoint, int expectedQuantity) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", equalTo(expectedQuantity)))
                .andReturn();
    }

    @Test
    @DisplayName("Debe completar flujo de venta basico")
    void testSimpleSalesFlow() throws Exception {

        categoryId = extractIdFromResponse(postRequest("/api/v1/categories",
                new CreateCategoryResource("Electronics", "Electronics products")));

        providerId = extractIdFromResponse(postRequest("/api/v1/providers",
                new CreateProviderResource("Miguel", "Suarez", "1234567890", "tech@email.com", "20123456789")));

        productId = extractIdFromResponse(postRequest("/api/v1/products",
                new CreateProductResource("USB Keyboard", "Mechanical RGB Keyboard",
                        String.valueOf(categoryId), String.valueOf(providerId), 5, 29.99, true)));

        Date now = new Date();
        Long batch1Id = extractIdFromResponse(postRequest("/api/v1/batches",
                new CreateBatchResource(productId, 50, addDays(25), now)));
        Long batch2Id = extractIdFromResponse(postRequest("/api/v1/batches",
                new CreateBatchResource(productId, 30, addDays(40), now)));

        postRequest("/api/v1/sales", new CreateSaleResource(1L, List.of(new ProductSaleItemResource(productId, 60)), null));

        // Se valida solo el resultado clave del flujo
        getAndValidate("/api/v1/batches/" + batch1Id, 0);
        getAndValidate("/api/v1/batches/" + batch2Id, 20);

        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

    }
}
