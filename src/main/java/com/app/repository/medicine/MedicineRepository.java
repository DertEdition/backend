package com.app.repository.medicine;

import com.app.model.entity.Medicine;
import com.app.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByUserId(Long userId);
    List<Medicine> findByDayOfWeek(DayOfWeek dayOfWeek);

    @Query("SELECT m FROM Medicine m WHERE m.user.id = :userId AND (m.dayOfWeek = :dayOfWeek OR m.dayOfWeek = 'EVERYDAY')")
    List<Medicine> findByUserIdAndDayOfWeekIncludingEveryday(Long userId, DayOfWeek dayOfWeek);
}