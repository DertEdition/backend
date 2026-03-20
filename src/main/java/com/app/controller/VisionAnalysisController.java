package com.app.controller;

import com.app.model.dto.VisionAnalysisResponse;
import com.app.service.VisionAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VisionAnalysisController {

    private final VisionAnalysisService visionAnalysisService;

    @PostMapping("/analyze-url")
    public ResponseEntity<?> performAnalysisByUrl(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");

        if (imageUrl == null || imageUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Görüntü URL'si boş olamaz.");
        }

        VisionAnalysisResponse response = visionAnalysisService.analyzeImageUrl(imageUrl);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Analiz yapılamadı.");
        }

        return ResponseEntity.ok(response);
    }
}