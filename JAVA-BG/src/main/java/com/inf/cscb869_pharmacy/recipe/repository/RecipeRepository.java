package com.inf.cscb869_pharmacy.recipe.repository;

import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // Existing methods
    List<Recipe> findAllByCreationDateAndDoctorId(LocalDate dateCreation, long doctorId);
    List<Recipe> findAllByCreationDateAndDoctorNameContains(LocalDate dateCreation, String doctorName);
    List<Recipe> findAllByCreationDateAndDoctorNameStartsWith(LocalDate dateCreation, String doctorName);

    /**
     * Find all recipes with a specific diagnosis (case-insensitive)
     */
    List<Recipe> findByDiagnosisContainingIgnoreCase(String diagnosis);

    /**
     * Count distinct patients with a specific diagnosis
     */
    @Query("SELECT COUNT(DISTINCT r.customer) FROM Recipe r WHERE LOWER(r.diagnosis) LIKE LOWER(CONCAT('%', :diagnosis, '%'))")
    long countDistinctPatientsByDiagnosis(@Param("diagnosis") String diagnosis);

    /**
     * Count recipes/examinations that have non-empty diagnosis text
     */
    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.diagnosis IS NOT NULL AND TRIM(r.diagnosis) <> ''")
    long countWithDiagnosis();

    /**
     * Find all recipes by doctor ID
     */
    List<Recipe> findByDoctorId(Long doctorId);

    /**
     * Count visits by doctor ID
     */
    long countByDoctorId(Long doctorId);

    /**
     * Find patient's medical history (all recipes) ordered by date descending
     */
    List<Recipe> findByCustomerIdOrderByCreationDateDesc(Long customerId);

    /**
     * Find all recipes with sick leave issued
     */
    List<Recipe> findBySickLeaveTrue();

    /**
     * Find sick leaves for a specific patient
     */
    List<Recipe> findByCustomerIdAndSickLeaveTrueOrderByCreationDateDesc(Long customerId);

    /**
     * Count sick leaves issued by each doctor
     */
    @Query("SELECT r.doctor.name, COUNT(r) FROM Recipe r WHERE r.sickLeave = TRUE GROUP BY r.doctor.id, r.doctor.name ORDER BY COUNT(r) DESC")
    List<Object[]> countSickLeavesByDoctor();

    /**
     * Find most common diagnoses with count
     */
    @Query("SELECT r.diagnosis, COUNT(r) as cnt FROM Recipe r WHERE r.diagnosis IS NOT NULL AND r.diagnosis != '' GROUP BY r.diagnosis ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiagnoses();

    /**
     * Find recipes in date range
     */
    List<Recipe> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find recipes by doctor in date range
     */
    List<Recipe> findByDoctorIdAndCreationDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * Count sick leaves by month
     */
    @Query(value = "SELECT EXTRACT(YEAR FROM r.creation_date) AS year, " +
            "EXTRACT(MONTH FROM r.creation_date) AS month, " +
            "COUNT(*) AS cnt " +
            "FROM recipe r " +
            "WHERE r.sick_leave = TRUE " +
            "GROUP BY EXTRACT(YEAR FROM r.creation_date), EXTRACT(MONTH FROM r.creation_date) " +
            "ORDER BY EXTRACT(YEAR FROM r.creation_date) DESC, EXTRACT(MONTH FROM r.creation_date) DESC",
            nativeQuery = true)
    List<Object[]> countSickLeavesByMonth();
}
