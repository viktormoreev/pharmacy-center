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

    /**
     * Find by unique leave number
     */
    Optional<SickLeave> findByLeaveNumber(String leaveNumber);

    /**
     * Find all sick leaves for a specific patient
     */
    List<SickLeave> findByCustomerIdOrderByStartDateDesc(Long customerId);

    /**
     * Find all sick leaves issued by a specific doctor
     */
    List<SickLeave> findByDoctorIdOrderByIssueDateDesc(Long doctorId);

    /**
     * Find all sick leaves for a specific recipe
     */
    List<SickLeave> findByRecipeId(Long recipeId);

    /**
     * Find sick leaves by status
     */
    List<SickLeave> findByStatus(SickLeaveStatus status);

    /**
     * Find active sick leaves for a patient
     */
    @Query("SELECT sl FROM SickLeave sl WHERE sl.customer.id = :customerId AND sl.status = 'ACTIVE' AND sl.endDate >= CURRENT_DATE ORDER BY sl.startDate DESC")
    List<SickLeave> findActiveSickLeavesByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find sick leaves in date range
     */
    List<SickLeave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count sick leaves by doctor
     */
    @Query("SELECT sl.doctor.name, COUNT(sl) FROM SickLeave sl GROUP BY sl.doctor.id, sl.doctor.name ORDER BY COUNT(sl) DESC")
    List<Object[]> countSickLeavesByDoctor();

    /**
     * Count sick leaves by month
     */
    @Query(value = "SELECT EXTRACT(YEAR FROM sl.start_date) AS yr, " +
            "EXTRACT(MONTH FROM sl.start_date) AS mn, " +
            "COUNT(*) AS cnt " +
            "FROM sick_leave sl " +
            "GROUP BY EXTRACT(YEAR FROM sl.start_date), EXTRACT(MONTH FROM sl.start_date) " +
            "ORDER BY EXTRACT(YEAR FROM sl.start_date) DESC, EXTRACT(MONTH FROM sl.start_date) DESC",
            nativeQuery = true)
    List<Object[]> countSickLeavesByMonth();

    /**
     * Check if patient has active sick leave on specific date
     */
    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END " +
           "FROM SickLeave sl " +
           "WHERE sl.customer.id = :customerId " +
           "AND sl.status = 'ACTIVE' " +
           "AND :date BETWEEN sl.startDate AND sl.endDate")
    boolean hasActiveSickLeaveOnDate(@Param("customerId") Long customerId, @Param("date") LocalDate date);
}
