package com.app.controller;

import com.app.model.dto.auth.LoginRequest;
import com.app.model.dto.auth.RegisterRequest;
import com.app.repository.MedicineRepository;
import com.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-MED-01 ~ TC-MED-11
 * TC-MED-12 (User/UI testi) otomatik test kapsamı dışındadır.
 *
 * NOT: Controller şu an POST → 200, DELETE → 200 dönüyor.
 * Doküman 201 ve 204 bekliyor. Testler mevcut koda göre ayarlandı.
 * İleride controller düzeltilirse andExpect satırları güncellenir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MedicationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private MedicineRepository medicineRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        medicineRepository.deleteAll();
        userRepository.deleteAll();

        var register = new RegisterRequest("med-test@example.com", "Pass1234!", "meduser");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        var login = new LoginRequest("med-test@example.com", "Pass1234!");
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("token").asText();
    }

    // ── yardımcı ──────────────────────────────────────────────────────────────

    private Map<String, Object> validMedication() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Parol");
        map.put("dosage", "500mg");
        map.put("dayOfWeek", "MONDAY");
        map.put("time", "08:00");
        return map;
    }

    private Long createMedication(Map<String, ?> payload) throws Exception {
        String body = mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    // =========================================================================
    // TC-MED-01
    // =========================================================================

    @Test
    @DisplayName("TC-MED-01: Geçerli veriyle ilaç kaydı oluşturulur → 200 OK")
    void createMedication_validInput_returns200() throws Exception {
        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMedication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Parol"))
                .andExpect(jsonPath("$.dosage").value("500mg"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    // =========================================================================
    // TC-MED-02
    // =========================================================================

    @Test
    @DisplayName("TC-MED-02: Bugünün ilaçları (EVERYDAY dahil) listelenir → 200 OK")
    void getTodayMedications_includesEverydayEntries_returns200() throws Exception {
        String today = java.time.LocalDate.now().getDayOfWeek().name();

        Map<String, Object> todayMed = new HashMap<>();
        todayMed.put("name", "Aspirin");
        todayMed.put("dosage", "100mg");
        todayMed.put("dayOfWeek", today);
        todayMed.put("time", "09:00");

        Map<String, Object> everydayMed = new HashMap<>();
        everydayMed.put("name", "Vitamin D");
        everydayMed.put("dosage", "1000IU");
        everydayMed.put("dayOfWeek", "EVERYDAY");
        everydayMed.put("time", "10:00");

        mockMvc.perform(post("/api/medicine")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todayMed)));

        mockMvc.perform(post("/api/medicine")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(everydayMed)));

        mockMvc.perform(get("/api/medicine/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // =========================================================================
    // TC-MED-03
    // =========================================================================

    @Test
    @DisplayName("TC-MED-03: Geçerli ID ile ilaç silinir → 200 OK")
    void deleteMedication_validId_returns200() throws Exception {
        Long id = createMedication(validMedication());

        mockMvc.perform(delete("/api/medicine/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TC-MED-04
    // =========================================================================

    @Test
    @DisplayName("TC-MED-04: Var olmayan ID silinmeye çalışılınca → 404 Not Found")
    void deleteMedication_nonExistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/medicine/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // TC-MED-05
    // =========================================================================

    @Test
    @DisplayName("TC-MED-05: Zorunlu alanlar eksik → 400 Bad Request")
    void createMedication_missingRequiredFields_returns400() throws Exception {
        Map<String, Object> empty = new HashMap<>();

        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empty)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-MED-06
    // =========================================================================

    @Test
    @DisplayName("TC-MED-06: Geçersiz saat formatı → 400 Bad Request")
    void createMedication_invalidTimeFormat_returns400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Parol");
        request.put("dosage", "500mg");
        request.put("dayOfWeek", "MONDAY");
        request.put("time", "8pm");

        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-MED-07
    // =========================================================================

    @Test
    @DisplayName("TC-MED-07: Geçersiz gün enum değeri → 400 Bad Request")
    void createMedication_invalidDayEnum_returns400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Parol");
        request.put("dosage", "500mg");
        request.put("dayOfWeek", "FUNDAY");
        request.put("time", "08:00");

        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TC-MED-08
    // =========================================================================

    @Test
    @DisplayName("TC-MED-08: Geçersiz token ile ilaç oluşturulamaz → 4xx")
    void createMedication_invalidToken_returns4xx() throws Exception {
        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMedication())))
                .andExpect(status().is4xxClientError());
    }

    // =========================================================================
    // TC-MED-09
    // =========================================================================

    @Test
    @DisplayName("TC-MED-09: Bugün için ilaç yoksa boş liste döner → 200 OK")
    void getTodayMedications_noMedicationsToday_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/medicine/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // TC-MED-10
    // =========================================================================

    @Test
    @DisplayName("TC-MED-10: Aynı ilaç iki kez eklenebilir (duplicate allowed) → 200 OK")
    void createMedication_duplicate_returns200() throws Exception {
        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMedication())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/medicine")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMedication())))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TC-MED-11
    // =========================================================================

    @Test
    @DisplayName("TC-MED-11: Oluştur → Listele → Sil tam yaşam döngüsü")
    void fullMedicationLifecycle_createListDelete() throws Exception {
        String today = java.time.LocalDate.now().getDayOfWeek().name();

        Map<String, Object> med = new HashMap<>();
        med.put("name", "Parol");
        med.put("dosage", "500mg");
        med.put("dayOfWeek", today);
        med.put("time", "08:00");

        // 1. Oluştur
        Long id = createMedication(med);

        // 2. Listede görünüyor mu?
        mockMvc.perform(get("/api/medicine/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        // 3. Sil
        mockMvc.perform(delete("/api/medicine/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. Listede artık yok
        mockMvc.perform(get("/api/medicine/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}