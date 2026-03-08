package com.app.controller;

import com.app.model.dto.MedicineRequest;
import com.app.model.entity.Medicine;
import com.app.model.entity.User;
import com.app.service.MedicineService;
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
    public ResponseEntity<Medicine> createMedicine(
            @RequestBody MedicineRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Medicine medicine = medicineService.createMedicine(request, currentUser);
        return ResponseEntity.ok(medicine);
    }

    @GetMapping
    public ResponseEntity<List<Medicine>> getUserMedicines(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(medicineService.getUserMedicines(currentUser.getId()));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Medicine>> getTodayMedicines(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(medicineService.getTodayMedicines(currentUser.getId()));
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long medicineId) {
        medicineService.deleteMedicine(medicineId);
        return ResponseEntity.ok().build();
    }
}