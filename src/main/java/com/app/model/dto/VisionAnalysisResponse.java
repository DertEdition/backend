package com.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisionAnalysisResponse {
    private String request_id;
    private boolean success;
    private String diagnosis_type;

    // AI servisinden gelen 'dermatology' objesi için
    private Map<String, Object> dermatology;

    private String explanation;
    private String confidence;
    private List<String> warnings;
    private String disclaimer;
    private double processing_time_ms;
}