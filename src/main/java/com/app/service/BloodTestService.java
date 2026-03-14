package com.app.service;

import com.app.model.entity.BloodTest;
import com.app.model.entity.User;
import com.app.repository.BloodTestRepository;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodTestService {

    private final BloodTestRepository bloodTestRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    private static final String AI_API_URL = "http://localhost:8081/analyze";

    @Transactional
    public BloodTest uploadAndAnalyze(MultipartFile file, User currentUser) {
        User managedUser = userRepository.getReferenceById(currentUser.getId());

        String s3Url = s3Service.uploadFile(file, currentUser.getId().toString(), "blood-test");

        String aiResultJson = sendToAIModel(file);

        BloodTest bloodTest = BloodTest.builder()
                .user(managedUser)
                .fileUrl(s3Url)
                .fileName(file.getOriginalFilename())
                .aiComment(aiResultJson)
                .build();

        return bloodTestRepository.save(bloodTest);
    }

    public List<BloodTest> getUserBloodTests(Long userId) {
        return bloodTestRepository.findByUserIdOrderByUploadDateDesc(userId);
    }

    public BloodTest getBloodTestById(Long id, Long userId) {
        return bloodTestRepository.findById(id)
                .filter(bt -> bt.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Kan testi bulunamadı"));
    }

    @Transactional
    public void deleteBloodTest(Long id, Long userId) {
        BloodTest bloodTest = getBloodTestById(id, userId);
        bloodTestRepository.delete(bloodTest);
    }

    private String sendToAIModel(MultipartFile file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(fileResource, createFileHeaders(file)));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    AI_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI analiz yanıtı alındı");
                return response.getBody();
            }

            log.warn("AI modelinden başarısız yanıt: {}", response.getStatusCode());
            return "{\"status\":\"error\",\"message\":\"AI modelinden yanıt alınamadı\"}";

        } catch (Exception e) {
            log.error("AI modeline bağlanılamadı: {}", e.getMessage());
            return "{\"status\":\"error\",\"message\":\"AI modeline bağlanılamadı: " + e.getMessage() + "\"}";
        }
    }

    private HttpHeaders createFileHeaders(MultipartFile file) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.valueOf(
                file.getContentType() != null ? file.getContentType() : "application/pdf"
        ));
        return fileHeaders;
    }
}
