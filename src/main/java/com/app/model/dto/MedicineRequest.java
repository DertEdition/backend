package com.app.model.dto;

import com.app.model.enums.DayOfWeek;
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
    private String name;
    private String dosage;
    private DayOfWeek dayOfWeek;
    private String time;
}