package com.app.service;

import com.app.model.dto.VisionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class VisionAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PYTHON_BASE_URL = "http://localhost:8000/analyze/medical";
    private static final String XRAY_ENDPOINT = PYTHON_BASE_URL + "/chest-xray/upload";
    private static final String DERMATOLOGY_ENDPOINT = PYTHON_BASE_URL + "/dermatology/upload";

    public VisionAnalysisResponse analyzeXrayImage(String imageUrl) {
        return processImageAnalysis(imageUrl, XRAY_ENDPOINT);
    }

    public VisionAnalysisResponse analyzeDermatologyImage(String imageUrl) {
        return processImageAnalysis(imageUrl, DERMATOLOGY_ENDPOINT);
    }

    public VisionAnalysisResponse analyzeImageUrl(String imageUrl) {
        return analyzeDermatologyImage(imageUrl);
    }

    private VisionAnalysisResponse processImageAnalysis(String imageUrl, String targetApiUrl) {
        try {
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes == null) return null;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "medical_analysis_image.jpg";
                }
            };

            body.add("file", resource);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(targetApiUrl, requestEntity, VisionAnalysisResponse.class);

        } catch (Exception e) {
            System.err.println("❌ AI Analysis Error on URL [" + targetApiUrl + "]: " + e.getMessage());
            return null;
        }
    }
}