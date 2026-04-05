package com.roommatematch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatematch.dto.request.ListingRequest;
import com.roommatematch.dto.request.RegisterRequest;
import com.roommatematch.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateUniqueEmail() {
        return "listing" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private RegisterRequest createRegisterRequest(String email, UserRole role) {
        return RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("password123")
                .role(role)
                .build();
    }

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequest request = createRegisterRequest(email, role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    private ListingRequest createListingRequest() {
        return ListingRequest.builder()
                .title("Test Listing")
                .description("A beautiful apartment for rent")
                .address("123 Test Street")
                .city("Riga")
                .country("Latvia")
                .monthlyRent(new BigDecimal("500.00"))
                .availableFrom(LocalDate.now().plusDays(30))
                .totalRooms(3)
                .availableRooms(1)
                .petsAllowed(false)
                .smokingAllowed(false)
                .build();
    }

    @Test
    @DisplayName("GET /api/listings/public returns 200 without auth")
    void testPublicListingsNoAuthReturns200() throws Exception {
        mockMvc.perform(get("/api/listings/public"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/listings without auth returns 403")
    void testCreateListingNoAuthReturns403() throws Exception {
        ListingRequest request = createListingRequest();

        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/listings with TENANT role returns 401")
    void testCreateListingTenantReturns401() throws Exception {
        String email = generateUniqueEmail();
        String token = registerAndGetToken(email, UserRole.TENANT);
        ListingRequest request = createListingRequest();

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/listings with LANDLORD role returns 201")
    void testCreateListingLandlordReturns201() throws Exception {
        String email = generateUniqueEmail();
        String token = registerAndGetToken(email, UserRole.LANDLORD);
        ListingRequest request = createListingRequest();

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
