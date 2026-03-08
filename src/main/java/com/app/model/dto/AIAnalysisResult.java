package com.app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    private String status;
    private String message;
    private List<String> anormallikler;
    private String rapor;

    @JsonProperty("tablo_sayisi")
    private Integer tabloSayisi;
}
