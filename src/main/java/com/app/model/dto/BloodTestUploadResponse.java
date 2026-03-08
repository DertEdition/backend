package com.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodTestUploadResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String aiComment;
    private String uploadDate;
}
