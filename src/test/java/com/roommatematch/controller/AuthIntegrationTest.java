package com.roommatematch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatematch.dto.request.LoginRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateUniqueEmail() {
        return "test" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
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

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = createRegisterRequest(email, UserRole.TENANT);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @DisplayName("POST /api/auth/register returns 201")
    void testRegisterReturns201() throws Exception {
        String email = generateUniqueEmail();
        RegisterRequest request = createRegisterRequest(email, UserRole.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("POST /api/auth/register duplicate email returns 409")
    void testRegisterDuplicateReturns409() throws Exception {
        String email = generateUniqueEmail();
        RegisterRequest request = createRegisterRequest(email, UserRole.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/login valid credentials returns 200")
    void testLoginReturns200() throws Exception {
        String email = generateUniqueEmail();
        RegisterRequest registerRequest = createRegisterRequest(email, UserRole.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/login wrong password returns 401")
    void testLoginWrongPasswordReturns401() throws Exception {
        String email = generateUniqueEmail();
        RegisterRequest registerRequest = createRegisterRequest(email, UserRole.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/me without token returns 403")
    void testProtectedEndpointWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/me with valid token returns 200")
    void testProtectedEndpointWithTokenReturns200() throws Exception {
        String email = generateUniqueEmail();
        String token = registerAndGetToken(email);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }
}
