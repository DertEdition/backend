package com.app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodTestDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String uploadDate;

    // AI analiz sonuçları
    private String status;
    private String message;
    private List<String> anormallikler;
    private String rapor;

    @JsonProperty("tablo_sayisi")
    private Integer tabloSayisi;
}
