package com.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI modeline kan testi belgesini gönderip analiz sonucu alan servis.
 * Şimdilik stub implementasyon — gerçek AI endpoint bağlandığında güncellenir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BloodTestAIService {

    @Value("${ai.blood-test.api-url:}")
    private String aiApiUrl;

    public String analyzeBloodTest(String fileUrl) {
        if (aiApiUrl == null || aiApiUrl.isBlank()) {
            log.info("AI API URL yapılandırılmamış, stub yanıt döndürülüyor. fileUrl={}", fileUrl);
            return "AI analizi henüz yapılandırılmadı. Lütfen daha sonra tekrar deneyin.";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            var request = new java.util.HashMap<String, String>();
            request.put("fileUrl", fileUrl);

            var response = restTemplate.postForObject(aiApiUrl, request, AIAnalysisResponse.class);

            if (response != null && response.getResult() != null) {
                return response.getResult();
            }

            return "AI modelinden yanıt alınamadı.";
        } catch (Exception e) {
            log.error("AI analizi sırasında hata oluştu: {}", e.getMessage(), e);
            return "AI analizi sırasında bir hata oluştu: " + e.getMessage();
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AIAnalysisResponse {
        private String result;
    }
}
