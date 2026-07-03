package com.inventiapp.stocktrack.iam.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventiapp.stocktrack.iam.domain.model.valueobjects.PermissionName;
import com.inventiapp.stocktrack.iam.interfaces.rest.resources.CreateUserResource;
import com.inventiapp.stocktrack.iam.interfaces.rest.resources.SignInResource;
import com.inventiapp.stocktrack.iam.interfaces.rest.resources.SignUpResource;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for IAM security on inventory product endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("IAM + Inventory integration - product endpoint authorization")
class IamInventoryIntegrationTest {

    private static final String PRODUCTS_ENDPOINT = "/api/v1/products";
    private static final String AUTH_SIGN_UP_ENDPOINT = "/api/v1/authentication/sign-up";
    private static final String AUTH_SIGN_IN_ENDPOINT = "/api/v1/authentication/sign-in";
    private static final String USERS_ENDPOINT = "/api/v1/users";

    private static final String ADMIN_PASSWORD = "AdminSecure#2026";
    private static final String STAFF_PASSWORD = "StaffSecure#2026";

    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Endpoint protegido sin token retorna 401")
    void should_ReturnUnauthorized_when_ProductsEndpointCalledWithoutToken() throws Exception {
        // Arrange
        // No authentication header

        // Act
        MvcResult result = mockMvc.perform(get(PRODUCTS_ENDPOINT)).andReturn();

        // Assert
        assertEquals(HTTP_UNAUTHORIZED, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Token válido con permisos de inventario retorna 200")
    void should_ReturnOkWithProducts_when_ValidTokenWithInventoryAccess() throws Exception {
        // Arrange
        String adminEmail = uniqueEmail("admin.inventario");
        String adminToken = signUpAndExtractToken(adminEmail, ADMIN_PASSWORD);

        // Act
        MvcResult result = mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        // Assert
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getStatus() == HTTP_OK && responseBody.isArray());
    }

    @Test
    @DisplayName("Token sin permiso de inventario retorna 403")
    void should_ReturnForbidden_when_TokenWithoutInventoryPermission() throws Exception {
        // Arrange
        String adminEmail = uniqueEmail("admin.seguridad");
        String adminToken = signUpAndExtractToken(adminEmail, ADMIN_PASSWORD);

        String staffEmail = uniqueEmail("cajero.ventas");
        String staffPassword = STAFF_PASSWORD;
        createStaffUserWithSalesAccessOnly(adminToken, staffEmail, staffPassword);
        String staffToken = signInAndExtractToken(staffEmail, staffPassword);

        // Act
        MvcResult result = mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", bearer(staffToken)))
                .andReturn();

        // Assert
        assertEquals(HTTP_FORBIDDEN, result.getResponse().getStatus());
    }

    private void createStaffUserWithSalesAccessOnly(String adminToken, String staffEmail, String staffPassword)
            throws Exception {
        mockMvc.perform(post(USERS_ENDPOINT)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserResource(
                                        staffEmail,
                                        staffPassword,
                                        List.of(PermissionName.SALES_ACCESS.getName())))))
                .andExpect(status().isCreated());
    }

    private String signUpAndExtractToken(String email, String password) throws Exception {
        MvcResult signUpResult = mockMvc.perform(post(AUTH_SIGN_UP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpResource(email, password))))
                .andExpect(status().isCreated())
                .andReturn();
        return extractToken(signUpResult.getResponse().getContentAsString());
    }

    private String signInAndExtractToken(String email, String password) throws Exception {
        MvcResult signInResult = mockMvc.perform(post(AUTH_SIGN_IN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignInResource(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return extractToken(signInResult.getResponse().getContentAsString());
    }

    private String extractToken(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@stocktrack.test";
    }
}
