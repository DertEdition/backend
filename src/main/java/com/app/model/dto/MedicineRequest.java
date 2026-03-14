package com.app.model.dto;

import com.app.model.enums.DayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    private Long userId;

    @NotBlank(message = "İlaç adı boş olamaz")
    private String name;

    @NotBlank(message = "Dozaj bilgisi boş olamaz")
    private String dosage;

    @NotNull(message = "Gün seçimi zorunludur")
    private DayOfWeek dayOfWeek;

    @NotBlank(message = "Saat alanı boş olamaz")
    @Pattern(
            regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
            message = "Saat formatı HH:mm (00:00 - 23:59) arasında olmalıdır"
    )
    private String time;
}