package com.inf.cscb869_pharmacy.diagnosis.repository;

import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    /**
     * Find all diagnoses for a specific recipe
     */
    List<Diagnosis> findByRecipeId(Long recipeId);

    /**
     * Find all diagnoses for a specific patient
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.recipe.customer.id = :customerId ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find diagnoses by name (case-insensitive)
     */
    List<Diagnosis> findByNameContainingIgnoreCase(String name);

    /**
     * Find most common diagnoses
     */
    @Query("SELECT d.name, COUNT(d) as cnt FROM Diagnosis d GROUP BY d.name ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiagnoses();

    /**
     * Count patients with specific diagnosis name
     */
    @Query("SELECT COUNT(DISTINCT d.recipe.customer) FROM Diagnosis d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :diagnosisName, '%'))")
    long countDistinctPatientsByDiagnosisName(@Param("diagnosisName") String diagnosisName);

    /**
     * Find diagnoses in date range
     */
    List<Diagnosis> findByDiagnosisDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find diagnoses by ICD-10 code
     */
    List<Diagnosis> findByIcd10Code(String icd10Code);

    /**
     * Find primary diagnoses only
     */
    List<Diagnosis> findByIsPrimaryTrue();
}
