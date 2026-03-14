package com.app.controller;

import com.app.model.dto.HealthProfileRequest;
import com.app.model.dto.auth.LoginRequest;
import com.app.model.dto.auth.RegisterRequest;
import com.app.repository.UserDetailsRepository;
import com.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-BMI-01 ~ TC-BMI-11
 * TC-BMI-12 (User/UI testi) otomatik test kapsamı dışındadır.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class HealthProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private UserDetailsRepository userDetailsRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        userDetailsRepository.deleteAll();
        userRepository.deleteAll();

        var register = new RegisterRequest("bmi-test@example.com", "Pass1234!", "bmiuser");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        var login = new LoginRequest("bmi-test@example.com", "Pass1234!");
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("token").asText();
    }

    // ── yardımcı ──────────────────────────────────────────────────────────────

    private HealthProfileRequest validRequest() {
        var r = new HealthProfileRequest();
        r.setWeight(70.0);
        r.setHeight(175.0);
        r.setWaist(80.0);
        r.setAge(25);
        r.setGender("MALE");
        return r;
    }

    // =========================================================================
    // TC-BMI-01
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-01: Geçerli girdilerle profil oluşturulur, metrikler döner")
    void createProfile_validInputs_returnsComputedMetrics() throws Exception {
        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").exists())
                .andExpect(jsonPath("$.bodyFatPercentage").exists())
                .andExpect(jsonPath("$.idealWeight").exists());
    }

    // =========================================================================
    // TC-BMI-02
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-02: Mevcut profil GET ile başarılı döner")
    void getProfile_existingProfile_returns200() throws Exception {
        mockMvc.perform(post("/api/health-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())));

        mockMvc.perform(get("/api/health-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(70.0))
                .andExpect(jsonPath("$.height").value(175.0))
                .andExpect(jsonPath("$.bmi").exists());
    }

    // =========================================================================
    // TC-BMI-03
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-03: Profil güncellenir, metrikler yeniden hesaplanır")
    void updateProfile_modifiedValues_recalculatesMetrics() throws Exception {
        mockMvc.perform(post("/api/health-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())));

        var updated = new HealthProfileRequest();
        updated.setWeight(80.0);
        updated.setHeight(180.0);
        updated.setWaist(85.0);
        updated.setAge(30);
        updated.setGender("MALE");

        String body = mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(80.0))
                .andExpect(jsonPath("$.height").value(180.0))
                .andReturn().getResponse().getContentAsString();

        // 80 / (1.8^2) = 24.69 → 24.7
        double bmi = objectMapper.readTree(body).get("bmi").asDouble();
        assertThat(bmi).isCloseTo(24.7, within(0.5));
    }

    // =========================================================================
    // TC-BMI-04
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-04: Negatif veya sıfır ağırlık/boy → 400 Bad Request")
    void createProfile_negativeOrZeroValues_returns400() throws Exception {
        var request = new HealthProfileRequest();
        request.setWeight(-5.0);
        request.setHeight(0.0);
        request.setWaist(80.0);
        request.setAge(25);
        request.setGender("MALE");

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-BMI-05
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-05: Aşırı değerler reddedilir (boy > 300 veya ağırlık > 500) → 400")
    void createProfile_extremeValues_returns400() throws Exception {
        var request = new HealthProfileRequest();
        request.setWeight(501.0);
        request.setHeight(301.0);
        request.setWaist(80.0);
        request.setAge(25);
        request.setGender("MALE");

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-BMI-06
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-06: Zorunlu alanlar eksik → 400 Bad Request")
    void createProfile_missingRequiredFields_returns400() throws Exception {
        var request = new HealthProfileRequest();
        request.setWaist(80.0);
        request.setAge(25);
        request.setGender("MALE");
        // weight ve height kasıtlı olarak null

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-BMI-07
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-07: Geçersiz yaş (< 1 veya > 150) → 400 Bad Request")
    void createProfile_invalidAge_returns400() throws Exception {
        var request = validRequest();
        request.setAge(0);

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        request.setAge(151);

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-BMI-08
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-08: Boş veya geçersiz cinsiyet → 400 Bad Request")
    void createProfile_invalidGender_returns400() throws Exception {
        var request = validRequest();
        request.setGender("");

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        request.setGender("GECERSIZ");

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-BMI-09
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-09: BMI formülü matematiksel olarak doğru sonuç üretir")
    void bmiCalculation_correctFormula() throws Exception {
        // Beklenen: 70 / (1.75^2) = 22.857 → 22.9
        String body = mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        double bmi = objectMapper.readTree(body).get("bmi").asDouble();
        assertThat(bmi).isCloseTo(22.9, within(0.2));
    }

    // =========================================================================
    // TC-BMI-10
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-10: Vücut yağı ve ideal kilo hesaplamaları tolerans içinde doğru")
    void auxiliaryCalculations_withinAcceptableTolerance() throws Exception {
        String body = mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var root = objectMapper.readTree(body);

        // Vücut yağı: 64 - (20 * (175 / 80)) = 20.25 → 20.3
        double bodyFat = root.get("bodyFatPercentage").asDouble();
        assertThat(bodyFat).isCloseTo(20.3, within(0.5));

        // İdeal kilo: round(22 * (1.75^2)) = 67
        double idealWeight = root.get("idealWeight").asDouble();
        assertThat(idealWeight).isCloseTo(67.0, within(1.0));
    }

    // =========================================================================
    // TC-BMI-11
    // =========================================================================

    @Test
    @DisplayName("TC-BMI-11: Boy=0 girilince hesaplama yapılmaz, 400 döner")
    void calculation_heightZero_preventsDivisionByZero() throws Exception {
        var request = validRequest();
        request.setHeight(0.0);

        mockMvc.perform(post("/api/health-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}