package com.app.service;

import com.app.model.dto.medicine.MedicineRequest;
import com.app.model.entity.Medicine;
import com.app.model.entity.User;
import com.app.model.enums.DayOfWeek;
import com.app.repository.medicine.MedicineRepository;
import com.app.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Transactional
    public Medicine createMedicine(MedicineRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Medicine medicine = Medicine.builder()
                .user(user)
                .name(request.getName())
                .dosage(request.getDosage())
                .dayOfWeek(request.getDayOfWeek())
                .time(LocalTime.parse(request.getTime()))
                .build();

        return medicineRepository.save(medicine);
    }

    public List<Medicine> getUserMedicines(Long userId) {
        return medicineRepository.findByUserId(userId);
    }

    public List<Medicine> getTodayMedicines(Long userId) {
        java.time.DayOfWeek javaDayOfWeek = LocalDate.now().getDayOfWeek();
        DayOfWeek today = convertJavaDayToCustomDay(javaDayOfWeek);

        return medicineRepository.findByUserIdAndDayOfWeekIncludingEveryday(userId, today);
    }

    @Transactional
    public void deleteMedicine(Long medicineId) {
        medicineRepository.deleteById(medicineId);
    }

    private DayOfWeek convertJavaDayToCustomDay(java.time.DayOfWeek javaDay) {
        return switch (javaDay) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }
}