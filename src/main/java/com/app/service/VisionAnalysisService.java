package com.app.service;

import com.app.model.dto.VisionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VisionAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.analysis.api-url:http://localhost:8082/analyze/medical/dermatology/upload}")
    private String aiApiUrl;

    public VisionAnalysisResponse analyzeImageUrl(String imageUrl) {
        try {
            RestTemplate rest = new RestTemplate();

            // 1. Resmi S3 URL'inden byte dizisi olarak indir (CORS engeli yok!)
            byte[] imageBytes = rest.getForObject(imageUrl, byte[].class);

            if (imageBytes == null) return null;

            // 2. Python AI servisine göndermek için hazırlık yap
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Byte dizisini bir dosya gibi paketliyoruz
            ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "analysis_image.jpg";
                }
            };

            body.add("file", resource);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 3. Kendi Python API'ne (8000) gönder
            return restTemplate.postForObject(aiApiUrl, requestEntity, VisionAnalysisResponse.class);

        } catch (Exception e) {
            System.err.println("❌ Backend Image Fetch Error: " + e.getMessage());
            return null;
        }
    }
}