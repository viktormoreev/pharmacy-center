package com.inf.cscb869_pharmacy.sickleave.repository;

import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {

    Optional<SickLeave> findByLeaveNumber(String leaveNumber);

    List<SickLeave> findByRecipeCustomerIdOrderByStartDateDesc(Long customerId);

    List<SickLeave> findByRecipeDoctorIdOrderByIssueDateDesc(Long doctorId);

    List<SickLeave> findByRecipeId(Long recipeId);

    List<SickLeave> findByStatus(SickLeaveStatus status);

    @Query("SELECT sl FROM SickLeave sl WHERE sl.recipe.customer.id = :customerId AND sl.status = 'ACTIVE' AND sl.endDate >= CURRENT_DATE ORDER BY sl.startDate DESC")
    List<SickLeave> findActiveSickLeavesByCustomerId(@Param("customerId") Long customerId);

    List<SickLeave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT sl.recipe.doctor.name, COUNT(sl) FROM SickLeave sl GROUP BY sl.recipe.doctor.id, sl.recipe.doctor.name ORDER BY COUNT(sl) DESC")
    List<Object[]> countSickLeavesByDoctor();

    @Query(value = "SELECT EXTRACT(YEAR FROM sl.start_date) AS yr, " +
            "EXTRACT(MONTH FROM sl.start_date) AS mn, " +
            "COUNT(*) AS cnt " +
            "FROM sick_leaves sl " +
            "GROUP BY EXTRACT(YEAR FROM sl.start_date), EXTRACT(MONTH FROM sl.start_date) " +
            "ORDER BY EXTRACT(YEAR FROM sl.start_date) DESC, EXTRACT(MONTH FROM sl.start_date) DESC",
            nativeQuery = true)
    List<Object[]> countSickLeavesByMonth();

    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END " +
           "FROM SickLeave sl " +
           "WHERE sl.recipe.customer.id = :customerId " +
           "AND sl.status = 'ACTIVE' " +
           "AND :date BETWEEN sl.startDate AND sl.endDate")
    boolean hasActiveSickLeaveOnDate(@Param("customerId") Long customerId, @Param("date") LocalDate date);
}
