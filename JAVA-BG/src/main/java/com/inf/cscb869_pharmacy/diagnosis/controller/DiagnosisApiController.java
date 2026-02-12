package com.inf.cscb869_pharmacy.diagnosis.controller;

import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.diagnosis.entity.DiagnosisSeverity;
import com.inf.cscb869_pharmacy.diagnosis.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API Controller for Diagnosis management
 * REST API контролер за управление на диагнози
 */
@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
@Slf4j
public class DiagnosisApiController {

    private final DiagnosisService diagnosisService;

    @GetMapping
    public ResponseEntity<List<Diagnosis>> getAllDiagnoses() {
        log.info("API: Getting all diagnoses");
        return ResponseEntity.ok(diagnosisService.getAllDiagnoses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diagnosis> getDiagnosisById(@PathVariable Long id) {
        log.info("API: Getting diagnosis by ID: {}", id);
        return ResponseEntity.ok(diagnosisService.getDiagnosisById(id));
    }

    @PostMapping
    public ResponseEntity<Diagnosis> createDiagnosis(@RequestBody Diagnosis diagnosis) {
        log.info("API: Creating new diagnosis: {}", diagnosis.getName());
        return ResponseEntity.ok(diagnosisService.createDiagnosis(diagnosis));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diagnosis> updateDiagnosis(
            @PathVariable Long id,
            @RequestBody Diagnosis diagnosis) {
        log.info("API: Updating diagnosis with ID: {}", id);
        return ResponseEntity.ok(diagnosisService.updateDiagnosis(id, diagnosis));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id) {
        log.info("API: Deleting diagnosis with ID: {}", id);
        diagnosisService.deleteDiagnosis(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByRecipeId(@PathVariable Long recipeId) {
        log.info("API: Getting diagnoses for recipe ID: {}", recipeId);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByRecipeId(recipeId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByCustomerId(@PathVariable Long customerId) {
        log.info("API: Getting diagnoses for customer ID: {}", customerId);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByCustomerId(customerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Diagnosis>> searchDiagnoses(@RequestParam String name) {
        log.info("API: Searching diagnoses by name: {}", name);
        return ResponseEntity.ok(diagnosisService.searchDiagnosesByName(name));
    }

    @GetMapping("/icd10/{code}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByIcd10Code(@PathVariable String code) {
        log.info("API: Getting diagnoses by ICD-10 code: {}", code);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByIcd10Code(code));
    }

    @GetMapping("/primary")
    public ResponseEntity<List<Diagnosis>> getPrimaryDiagnoses() {
        log.info("API: Getting primary diagnoses");
        return ResponseEntity.ok(diagnosisService.getPrimaryDiagnoses());
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesBySeverity(@PathVariable DiagnosisSeverity severity) {
        log.info("API: Getting diagnoses with severity: {}", severity);
        return ResponseEntity.ok(diagnosisService.getDiagnosesBySeverity(severity));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Diagnosis>> getDiagnosesInDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("API: Getting diagnoses between {} and {}", startDate, endDate);
        return ResponseEntity.ok(diagnosisService.getDiagnosesInDateRange(startDate, endDate));
    }

    @GetMapping("/statistics/common")
    public ResponseEntity<List<Object[]>> getMostCommonDiagnoses() {
        log.info("API: Getting most common diagnoses");
        return ResponseEntity.ok(diagnosisService.getMostCommonDiagnoses());
    }
}
