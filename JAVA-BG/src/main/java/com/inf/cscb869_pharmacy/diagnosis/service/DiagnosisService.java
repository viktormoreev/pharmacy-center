package com.inf.cscb869_pharmacy.diagnosis.service;

import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.diagnosis.entity.DiagnosisSeverity;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Diagnosis management
 * Управление на диагнози
 */
public interface DiagnosisService {

    /**
     * Create a new diagnosis
     */
    Diagnosis createDiagnosis(Diagnosis diagnosis);

    /**
     * Update existing diagnosis
     */
    Diagnosis updateDiagnosis(Long id, Diagnosis diagnosis);

    /**
     * Delete diagnosis by ID
     */
    void deleteDiagnosis(Long id);

    /**
     * Get diagnosis by ID
     */
    Diagnosis getDiagnosisById(Long id);

    /**
     * Get all diagnoses
     */
    List<Diagnosis> getAllDiagnoses();

    /**
     * Get diagnoses for a specific recipe
     */
    List<Diagnosis> getDiagnosesByRecipeId(Long recipeId);

    /**
     * Get diagnoses for a specific patient/customer
     */
    List<Diagnosis> getDiagnosesByCustomerId(Long customerId);

    /**
     * Search diagnoses by name (partial match)
     */
    List<Diagnosis> searchDiagnosesByName(String name);

    /**
     * Get most common diagnoses with patient count
     */
    List<Object[]> getMostCommonDiagnoses();

    /**
     * Count distinct patients with a specific diagnosis
     */
    Long countPatientsByDiagnosisName(String diagnosisName);

    /**
     * Get diagnoses in date range
     */
    List<Diagnosis> getDiagnosesInDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get diagnoses by ICD-10 code
     */
    List<Diagnosis> getDiagnosesByIcd10Code(String icd10Code);

    /**
     * Get only primary diagnoses
     */
    List<Diagnosis> getPrimaryDiagnoses();

    /**
     * Get diagnoses by severity
     */
    List<Diagnosis> getDiagnosesBySeverity(DiagnosisSeverity severity);
}
