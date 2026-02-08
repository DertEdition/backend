package com.app.controller;

import com.app.model.dto.HealthProfileRequest;
import com.app.model.entity.User;
import com.app.service.HealthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Frontend bağlantısı için
public class HealthProfileController {

    private final HealthProfileService service;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdateProfile(
            @RequestBody HealthProfileRequest request,
            @AuthenticationPrincipal User currentUser) {

        Map<String, Object> response = service.processProfile(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> profile = service.getProfileByUserId(currentUser);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }
}