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

    Diagnosis createDiagnosis(Diagnosis diagnosis);

    Diagnosis updateDiagnosis(Long id, Diagnosis diagnosis);

    void deleteDiagnosis(Long id);

    Diagnosis getDiagnosisById(Long id);

    List<Diagnosis> getAllDiagnoses();

    List<Diagnosis> getDiagnosesByRecipeId(Long recipeId);

    List<Diagnosis> getDiagnosesByCustomerId(Long customerId);

    List<Diagnosis> searchDiagnosesByName(String name);

    List<Object[]> getMostCommonDiagnoses();

    Long countPatientsByDiagnosisName(String diagnosisName);

    List<Diagnosis> getDiagnosesInDateRange(LocalDate startDate, LocalDate endDate);

    List<Diagnosis> getDiagnosesByIcd10Code(String icd10Code);

    List<Diagnosis> getPrimaryDiagnoses();

    List<Diagnosis> getDiagnosesBySeverity(DiagnosisSeverity severity);
}
