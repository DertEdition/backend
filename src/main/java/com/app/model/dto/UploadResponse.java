package com.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private boolean success;
    private String fileUrl;
    private String bodyPart;
    private String uploadDate;
}
