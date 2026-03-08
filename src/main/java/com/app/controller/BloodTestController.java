package com.app.controller;

import com.app.model.dto.BloodTestDTO;
import com.app.model.entity.BloodTest;
import com.app.model.entity.User;
import com.app.service.BloodTestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blood-test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class BloodTestController {

    private final BloodTestService bloodTestService;
    private final ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<BloodTestDTO> uploadBloodTest(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        BloodTest saved = bloodTestService.uploadAndAnalyze(file, currentUser);
        return ResponseEntity.ok(toDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<BloodTestDTO>> getBloodTests(
            @AuthenticationPrincipal User currentUser
    ) {
        List<BloodTestDTO> dtos = bloodTestService.getUserBloodTests(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBloodTest(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        bloodTestService.deleteBloodTest(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    private BloodTestDTO toDTO(BloodTest bt) {
        BloodTestDTO dto = BloodTestDTO.builder()
                .id(bt.getId())
                .fileName(bt.getFileName())
                .fileUrl(bt.getFileUrl())
                .uploadDate(bt.getUploadDate().toString())
                .build();

        // aiComment'te saklanan JSON'u parse edip DTO alanlarına maple
        parseAiResponse(bt.getAiComment(), dto);
        return dto;
    }

    private void parseAiResponse(String aiCommentJson, BloodTestDTO dto) {
        if (aiCommentJson == null || aiCommentJson.isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(aiCommentJson);

            if (root.has("status")) {
                dto.setStatus(root.get("status").asText());
            }
            if (root.has("message")) {
                dto.setMessage(root.get("message").asText());
            }
            if (root.has("rapor")) {
                dto.setRapor(root.get("rapor").asText());
            }
            if (root.has("tablo_sayisi")) {
                dto.setTabloSayisi(root.get("tablo_sayisi").asInt());
            }
            if (root.has("anormallikler") && root.get("anormallikler").isArray()) {
                List<String> anormallikler = new java.util.ArrayList<>();
                for (JsonNode item : root.get("anormallikler")) {
                    anormallikler.add(item.asText());
                }
                dto.setAnormallikler(anormallikler);
            }
        } catch (Exception e) {
            log.warn("AI yanıtı JSON parse edilemedi: {}", e.getMessage());
            // Parse edilemezse rapor alanına ham metni koy
            dto.setRapor(aiCommentJson);
            dto.setStatus("error");
        }
    }
}
