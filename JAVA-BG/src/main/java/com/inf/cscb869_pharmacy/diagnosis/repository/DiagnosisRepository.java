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

    List<Diagnosis> findByRecipeId(Long recipeId);

    @Query("SELECT d FROM Diagnosis d WHERE d.recipe.customer.id = :customerId ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findByCustomerId(@Param("customerId") Long customerId);

    List<Diagnosis> findByNameContainingIgnoreCase(String name);

    @Query("SELECT d.name, COUNT(d) as cnt FROM Diagnosis d GROUP BY d.name ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiagnoses();

    @Query("SELECT COUNT(DISTINCT d.recipe.customer) FROM Diagnosis d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :diagnosisName, '%'))")
    long countDistinctPatientsByDiagnosisName(@Param("diagnosisName") String diagnosisName);

    List<Diagnosis> findByDiagnosisDateBetween(LocalDate startDate, LocalDate endDate);

    List<Diagnosis> findByIcd10Code(String icd10Code);

    List<Diagnosis> findByIsPrimaryTrue();
}
