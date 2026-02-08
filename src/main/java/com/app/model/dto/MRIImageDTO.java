package com.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MRIImageDTO {
    private Long id;
    private String fileUrl;
    private String bodyPart;
    private String fileType;
    private String uploadDate;
}