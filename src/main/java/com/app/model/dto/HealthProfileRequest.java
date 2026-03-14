package com.app.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileRequest {
    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("500")
    private Double weight;

    @NotNull @DecimalMin("1") @DecimalMax("300")
    private Double waist;

    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("300")
    private Double height;

    @NotNull @Min(1) @Max(150)
    private Integer age;

    @NotBlank
    @Pattern(regexp = "MALE|FEMALE")
    private String gender;
}