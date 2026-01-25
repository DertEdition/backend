package com.app.model.enums;

import lombok.Getter;

@Getter
public enum DayOfWeek {
    EVERYDAY(-1, "Her Gün"),
    SUNDAY(0, "Pazar"),
    MONDAY(1, "Pazartesi"),
    TUESDAY(2, "Salı"),
    WEDNESDAY(3, "Çarşamba"),
    THURSDAY(4, "Perşembe"),
    FRIDAY(5, "Cuma"),
    SATURDAY(6, "Cumartesi");

    private final int value;
    private final String displayName;

    DayOfWeek(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static DayOfWeek fromValue(int value) {
        for (DayOfWeek day : values()) {
            if (day.value == value) {
                return day;
            }
        }
        throw new IllegalArgumentException("Geçersiz gün değeri: " + value);
    }
}