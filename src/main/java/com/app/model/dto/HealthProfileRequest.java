package com.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileRequest {
    private Double weight;
    private Double height;
    private Double waist;
    private Integer age;
    private String gender;
}