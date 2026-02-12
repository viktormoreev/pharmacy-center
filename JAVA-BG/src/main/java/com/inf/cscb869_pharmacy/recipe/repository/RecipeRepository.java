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

    List<Recipe> findAllByCreationDateAndDoctorId(LocalDate dateCreation, long doctorId);
    List<Recipe> findAllByCreationDateAndDoctorNameContains(LocalDate dateCreation, String doctorName);
    List<Recipe> findAllByCreationDateAndDoctorNameStartsWith(LocalDate dateCreation, String doctorName);

    List<Recipe> findByDiagnosisContainingIgnoreCase(String diagnosis);

    @Query("SELECT COUNT(DISTINCT r.customer) FROM Recipe r WHERE LOWER(r.diagnosis) LIKE LOWER(CONCAT('%', :diagnosis, '%'))")
    long countDistinctPatientsByDiagnosis(@Param("diagnosis") String diagnosis);

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.diagnosis IS NOT NULL AND TRIM(r.diagnosis) <> ''")
    long countWithDiagnosis();

    List<Recipe> findByDoctorId(Long doctorId);

    long countByDoctorId(Long doctorId);

    List<Recipe> findByCustomerIdOrderByCreationDateDesc(Long customerId);

    List<Recipe> findBySickLeaveTrue();

    List<Recipe> findByCustomerIdAndSickLeaveTrueOrderByCreationDateDesc(Long customerId);

    @Query("SELECT r.doctor.name, COUNT(r) FROM Recipe r WHERE r.sickLeave = TRUE GROUP BY r.doctor.id, r.doctor.name ORDER BY COUNT(r) DESC")
    List<Object[]> countSickLeavesByDoctor();

    @Query("SELECT r.diagnosis, COUNT(r) as cnt FROM Recipe r WHERE r.diagnosis IS NOT NULL AND r.diagnosis != '' GROUP BY r.diagnosis ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiagnoses();

    List<Recipe> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    List<Recipe> findByDoctorIdAndCreationDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT EXTRACT(YEAR FROM r.creation_date) AS yr, " +
            "EXTRACT(MONTH FROM r.creation_date) AS mn, " +
            "COUNT(*) AS cnt " +
            "FROM recipe r " +
            "WHERE r.sick_leave = TRUE " +
            "GROUP BY EXTRACT(YEAR FROM r.creation_date), EXTRACT(MONTH FROM r.creation_date) " +
            "ORDER BY EXTRACT(YEAR FROM r.creation_date) DESC, EXTRACT(MONTH FROM r.creation_date) DESC",
            nativeQuery = true)
    List<Object[]> countSickLeavesByMonth();
}
