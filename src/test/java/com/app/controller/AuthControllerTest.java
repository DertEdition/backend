package com.app.controller;

import com.app.model.dto.auth.AuthResponse;
import com.app.model.dto.auth.LoginRequest;
import com.app.model.dto.auth.RegisterRequest;
import com.app.model.entity.User;
import com.app.repository.UserRepository;
import com.app.security.JwtAuthenticationFilter;
import com.app.security.JwtUtil;
import com.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    // =========================================================================
    // TC-AUTH-01 ~ TC-AUTH-07  Controller katmanı (WebMvcTest + Mock)
    // =========================================================================

    @Nested
    @DisplayName("Controller Tests (TC-AUTH-01 ~ 07)")
    @WebMvcTest(AuthController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class ControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private JwtUtil jwtUtil;

        @Test
        @DisplayName("TC-AUTH-01: Geçerli bilgilerle kayıt → 200 OK + token")
        void register_validCredentials_returns200() throws Exception {
            var userInfo = new AuthResponse.UserInfo(1L, "test@example.com", "tester");
            var response = new AuthResponse("mock-token", userInfo);
            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            var req = new RegisterRequest("test@example.com", "password123", "tester");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-token"))
                    .andExpect(jsonPath("$.user.userId").value(1));
        }

        @Test
        @DisplayName("TC-AUTH-02: Kayıtlı e-posta tekrar gönderilince → 409 Conflict")
        void register_duplicateEmail_returns409() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new IllegalStateException("Email kullanımda"));

            var req = new RegisterRequest("exists@example.com", "pwd", "user");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("TC-AUTH-03: Geçersiz e-posta formatı → 400 Bad Request")
        void register_invalidEmail_returns400() throws Exception {
            var req = new RegisterRequest("not-an-email", "pwd", "user");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC-AUTH-04: Zorunlu alanlar boş → 400 Bad Request")
        void register_emptyFields_returns400() throws Exception {
            var req = new RegisterRequest("", "", "");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC-AUTH-05: Geçerli kimlik bilgileriyle giriş → 200 OK + token")
        void login_validCredentials_returns200() throws Exception {
            var userInfo = new AuthResponse.UserInfo(2L, "login@example.com", "loginuser");
            var response = new AuthResponse("login-token", userInfo);
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            var req = new LoginRequest("login@example.com", "secret");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("login-token"));
        }

        @Test
        @DisplayName("TC-AUTH-06: Yanlış şifre → 401 Unauthorized")
        void login_incorrectPassword_returns401() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("Hatalı şifre"));

            var req = new LoginRequest("login@example.com", "wrong");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("TC-AUTH-07: Kayıtsız e-posta → 401 Unauthorized")
        void login_unregisteredEmail_returns401() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("Kullanıcı bulunamadı"));

            var req = new LoginRequest("nouser@example.com", "pwd");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // TC-AUTH-08  JWT token üretimi ve doğruluğu (Unit)
    // =========================================================================

    @Nested
    @DisplayName("JWT Unit Tests (TC-AUTH-08)")
    @SpringBootTest
    @TestPropertySource(properties = {
            "app.jwt.secret=012345678901234567890123456789012345678901234567890123",
            "app.jwt.expiration=3600000"
    })
    class JwtTests {

        @Autowired
        private JwtUtil jwtUtil;

        @Test
        @DisplayName("TC-AUTH-08: Token doğru subject ve geçerli expiry içeriyor")
        void generateToken_containsCorrectSubjectAndExpiry() {
            User user = new User();
            user.setEmail("test@jwt.com");
            user.setId(123L);

            String token = jwtUtil.generateToken(user);
            assertThat(token).isNotNull();

            String email = jwtUtil.getEmailFromToken(token);
            assertThat(email).isEqualTo("test@jwt.com");

            boolean valid = jwtUtil.validateToken(token);
            assertThat(valid).isTrue();
        }
    }

    // =========================================================================
    // TC-AUTH-09 ~ TC-AUTH-13  Entegrasyon ve uçtan uca testler (SpringBootTest)
    // =========================================================================

    @Nested
    @DisplayName("Integration & E2E Tests (TC-AUTH-09 ~ 13)")
    @SpringBootTest
    @AutoConfigureMockMvc
    @TestPropertySource(properties = {
            "app.jwt.secret=012345678901234567890123456789012345678901234567890123",
            "app.jwt.expiration=3600000"
    })
    class IntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void cleanup() {
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("TC-AUTH-09~13: Kayıt → Giriş → Korumalı erişim → Tokensiz erişim")
        void endToEnd_registration_login_protectedFlow() throws Exception {

            // TC-AUTH-13: Kayıt başarılı
            var registerReq = new RegisterRequest("inttest@example.com", "pass1234", "intuser");
            var regResult = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            assertThat(regResult.getResponse().getContentAsString()).contains("token");

            // TC-AUTH-13: Giriş başarılı, token döndü
            var loginReq = new LoginRequest("inttest@example.com", "pass1234");
            var loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginBody = loginResult.getResponse().getContentAsString();
            assertThat(loginBody).contains("token");

            // TC-AUTH-09: Geçerli JWT ile korumalı endpoint → 200 OK
            String token = objectMapper.readTree(loginBody).get("token").asText();
            assertThat(token).isNotBlank();

            mockMvc.perform(get("/api/blood-test")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // TC-AUTH-12: Authorization header yok → 403 Forbidden
            mockMvc.perform(get("/api/blood-test"))
                    .andExpect(status().isForbidden());
        }
    }
}