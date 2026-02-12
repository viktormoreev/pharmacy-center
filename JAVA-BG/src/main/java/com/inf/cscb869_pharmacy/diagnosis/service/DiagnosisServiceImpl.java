package com.inf.cscb869_pharmacy.diagnosis.service;

import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.diagnosis.entity.DiagnosisSeverity;
import com.inf.cscb869_pharmacy.diagnosis.repository.DiagnosisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Diagnosis management
 * Имплементация на услуги за управление на диагнози
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;

    @Override
    public Diagnosis createDiagnosis(Diagnosis diagnosis) {
        log.info("Creating new diagnosis: {}", diagnosis.getName());
        if (diagnosis.getDiagnosisDate() == null) {
            diagnosis.setDiagnosisDate(LocalDate.now());
        }
        return diagnosisRepository.save(diagnosis);
    }

    @Override
    public Diagnosis updateDiagnosis(Long id, Diagnosis diagnosis) {
        log.info("Updating diagnosis with ID: {}", id);
        Diagnosis existing = getDiagnosisById(id);

        existing.setIcd10Code(diagnosis.getIcd10Code());
        existing.setName(diagnosis.getName());
        existing.setDescription(diagnosis.getDescription());
        existing.setDiagnosisDate(diagnosis.getDiagnosisDate());
        existing.setIsPrimary(diagnosis.getIsPrimary());
        existing.setSeverity(diagnosis.getSeverity());
        existing.setNotes(diagnosis.getNotes());

        return diagnosisRepository.save(existing);
    }

    @Override
    public void deleteDiagnosis(Long id) {
        log.info("Deleting diagnosis with ID: {}", id);
        diagnosisRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Diagnosis getDiagnosisById(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnosis not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getAllDiagnoses() {
        log.info("Fetching all diagnoses");
        return diagnosisRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByRecipeId(Long recipeId) {
        log.info("Fetching diagnoses for recipe ID: {}", recipeId);
        return diagnosisRepository.findByRecipeId(recipeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByCustomerId(Long customerId) {
        log.info("Fetching diagnoses for customer ID: {}", customerId);
        return diagnosisRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> searchDiagnosesByName(String name) {
        log.info("Searching diagnoses by name: {}", name);
        return diagnosisRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostCommonDiagnoses() {
        log.info("Fetching most common diagnoses");
        return diagnosisRepository.findMostCommonDiagnoses();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPatientsByDiagnosisName(String diagnosisName) {
        log.info("Counting patients with diagnosis: {}", diagnosisName);
        return diagnosisRepository.countDistinctPatientsByDiagnosisName(diagnosisName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching diagnoses between {} and {}", startDate, endDate);
        return diagnosisRepository.findByDiagnosisDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByIcd10Code(String icd10Code) {
        log.info("Fetching diagnoses with ICD-10 code: {}", icd10Code);
        return diagnosisRepository.findByIcd10Code(icd10Code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getPrimaryDiagnoses() {
        log.info("Fetching all primary diagnoses");
        return diagnosisRepository.findByIsPrimaryTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesBySeverity(DiagnosisSeverity severity) {
        log.info("Fetching diagnoses with severity: {}", severity);
        return diagnosisRepository.findAll().stream()
                .filter(d -> d.getSeverity() == severity)
                .toList();
    }
}
