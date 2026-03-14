package com.app.controller;

import com.app.model.dto.MedicineRequest;
import com.app.model.entity.Medicine;
import com.app.model.entity.User;
import com.app.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicine")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    public ResponseEntity<?> createMedicine(@RequestBody MedicineRequest request, @AuthenticationPrincipal User user) {
        try {
            Medicine created = medicineService.createMedicine(request, user);
            return ResponseEntity.ok(created);
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Geçersiz saat formatı: " + e.getParsedString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        try {
            medicineService.deleteMedicine(id);
            return ResponseEntity.ok().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Medicine>> getUserMedicines(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(medicineService.getUserMedicines(currentUser.getId()));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Medicine>> getTodayMedicines(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(medicineService.getTodayMedicines(currentUser.getId()));
    }

}