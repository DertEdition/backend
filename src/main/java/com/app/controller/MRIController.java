package com.app.controller;

import com.app.model.dto.MRIImageDTO;
import com.app.model.dto.UploadResponse;
import com.app.model.entity.MRIImage;
import com.app.model.entity.User;
import com.app.repository.MRIRepository;
import com.app.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mri")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MRIController {

    private final S3Service s3Service;
    private final MRIRepository mriRepository;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadMRI(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bodyPart") String bodyPart,
            @AuthenticationPrincipal User currentUser
    ) {
        String s3Url = s3Service.uploadFile(file, currentUser.getId().toString(), bodyPart);

        MRIImage mriImage = new MRIImage();
        mriImage.setUser(currentUser);
        mriImage.setBodyPart(bodyPart);
        mriImage.setFileUrl(s3Url);
        mriImage.setFileType(file.getContentType());
        mriImage.setUploadDate(LocalDateTime.now());
        mriRepository.save(mriImage);

        UploadResponse response = new UploadResponse(
                true,
                s3Url,
                bodyPart,
                LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MRIImageDTO>> getMRIImages(
            @RequestParam(required = false) String bodyPart,
            @AuthenticationPrincipal User currentUser
    ) {
        List<MRIImage> images = bodyPart != null
                ? mriRepository.findByUserIdAndBodyPart(currentUser.getId(), bodyPart)
                : mriRepository.findByUserId(currentUser.getId());

        List<MRIImageDTO> dtos = images.stream()
                .map(img -> new MRIImageDTO(
                        img.getId(),
                        img.getFileUrl(),
                        img.getBodyPart(),
                        img.getFileType(),
                        img.getUploadDate().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
