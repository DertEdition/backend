package com.app.controller;

import com.app.model.dto.medicine.MedicineRequest;
import com.app.model.entity.Medicine;
import com.app.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicine")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    public ResponseEntity<Medicine> createMedicine(@RequestBody MedicineRequest request) {
        Medicine medicine = medicineService.createMedicine(request);
        return ResponseEntity.ok(medicine);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Medicine>> getUserMedicines(@PathVariable Long userId) {
        return ResponseEntity.ok(medicineService.getUserMedicines(userId));
    }

    @GetMapping("/today/{userId}")
    public ResponseEntity<List<Medicine>> getTodayMedicines(@PathVariable Long userId) {
        return ResponseEntity.ok(medicineService.getTodayMedicines(userId));
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long medicineId) {
        medicineService.deleteMedicine(medicineId);
        return ResponseEntity.ok().build();
    }
}