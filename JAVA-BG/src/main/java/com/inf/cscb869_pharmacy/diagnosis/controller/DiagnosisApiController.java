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

    /**
     * Get all diagnoses
     * GET /api/diagnoses
     */
    @GetMapping
    public ResponseEntity<List<Diagnosis>> getAllDiagnoses() {
        log.info("API: Getting all diagnoses");
        return ResponseEntity.ok(diagnosisService.getAllDiagnoses());
    }

    /**
     * Get diagnosis by ID
     * GET /api/diagnoses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Diagnosis> getDiagnosisById(@PathVariable Long id) {
        log.info("API: Getting diagnosis by ID: {}", id);
        return ResponseEntity.ok(diagnosisService.getDiagnosisById(id));
    }

    /**
     * Create new diagnosis
     * POST /api/diagnoses
     */
    @PostMapping
    public ResponseEntity<Diagnosis> createDiagnosis(@RequestBody Diagnosis diagnosis) {
        log.info("API: Creating new diagnosis: {}", diagnosis.getName());
        return ResponseEntity.ok(diagnosisService.createDiagnosis(diagnosis));
    }

    /**
     * Update diagnosis
     * PUT /api/diagnoses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Diagnosis> updateDiagnosis(
            @PathVariable Long id,
            @RequestBody Diagnosis diagnosis) {
        log.info("API: Updating diagnosis with ID: {}", id);
        return ResponseEntity.ok(diagnosisService.updateDiagnosis(id, diagnosis));
    }

    /**
     * Delete diagnosis
     * DELETE /api/diagnoses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id) {
        log.info("API: Deleting diagnosis with ID: {}", id);
        diagnosisService.deleteDiagnosis(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get diagnoses by recipe ID
     * GET /api/diagnoses/recipe/{recipeId}
     */
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByRecipeId(@PathVariable Long recipeId) {
        log.info("API: Getting diagnoses for recipe ID: {}", recipeId);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByRecipeId(recipeId));
    }

    /**
     * Get diagnoses by customer ID
     * GET /api/diagnoses/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByCustomerId(@PathVariable Long customerId) {
        log.info("API: Getting diagnoses for customer ID: {}", customerId);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByCustomerId(customerId));
    }

    /**
     * Search diagnoses by name
     * GET /api/diagnoses/search?name=грип
     */
    @GetMapping("/search")
    public ResponseEntity<List<Diagnosis>> searchDiagnoses(@RequestParam String name) {
        log.info("API: Searching diagnoses by name: {}", name);
        return ResponseEntity.ok(diagnosisService.searchDiagnosesByName(name));
    }

    /**
     * Get diagnoses by ICD-10 code
     * GET /api/diagnoses/icd10/{code}
     */
    @GetMapping("/icd10/{code}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesByIcd10Code(@PathVariable String code) {
        log.info("API: Getting diagnoses by ICD-10 code: {}", code);
        return ResponseEntity.ok(diagnosisService.getDiagnosesByIcd10Code(code));
    }

    /**
     * Get primary diagnoses only
     * GET /api/diagnoses/primary
     */
    @GetMapping("/primary")
    public ResponseEntity<List<Diagnosis>> getPrimaryDiagnoses() {
        log.info("API: Getting primary diagnoses");
        return ResponseEntity.ok(diagnosisService.getPrimaryDiagnoses());
    }

    /**
     * Get diagnoses by severity
     * GET /api/diagnoses/severity/{severity}
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Diagnosis>> getDiagnosesBySeverity(@PathVariable DiagnosisSeverity severity) {
        log.info("API: Getting diagnoses with severity: {}", severity);
        return ResponseEntity.ok(diagnosisService.getDiagnosesBySeverity(severity));
    }

    /**
     * Get diagnoses in date range
     * GET /api/diagnoses/date-range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Diagnosis>> getDiagnosesInDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("API: Getting diagnoses between {} and {}", startDate, endDate);
        return ResponseEntity.ok(diagnosisService.getDiagnosesInDateRange(startDate, endDate));
    }

    /**
     * Get most common diagnoses
     * GET /api/diagnoses/statistics/common
     */
    @GetMapping("/statistics/common")
    public ResponseEntity<List<Object[]>> getMostCommonDiagnoses() {
        log.info("API: Getting most common diagnoses");
        return ResponseEntity.ok(diagnosisService.getMostCommonDiagnoses());
    }
}
