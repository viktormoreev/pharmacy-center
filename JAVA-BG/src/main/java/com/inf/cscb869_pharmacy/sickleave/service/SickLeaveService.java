package com.inf.cscb869_pharmacy.sickleave.service;

import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Sick Leave management
 * Управление на болнични листове
 */
public interface SickLeaveService {

    /**
     * Create a new sick leave
     */
    SickLeave createSickLeave(SickLeave sickLeave);

    /**
     * Update existing sick leave
     */
    SickLeave updateSickLeave(Long id, SickLeave sickLeave);

    /**
     * Delete sick leave by ID
     */
    void deleteSickLeave(Long id);

    /**
     * Get sick leave by ID
     */
    SickLeave getSickLeaveById(Long id);

    /**
     * Get all sick leaves
     */
    List<SickLeave> getAllSickLeaves();

    /**
     * Get sick leave by unique leave number
     */
    SickLeave getSickLeaveByNumber(String leaveNumber);

    /**
     * Get sick leaves for a specific patient
     */
    List<SickLeave> getSickLeavesByCustomerId(Long customerId);

    /**
     * Get sick leaves issued by a specific doctor
     */
    List<SickLeave> getSickLeavesByDoctorId(Long doctorId);

    /**
     * Get sick leaves for a specific recipe
     */
    List<SickLeave> getSickLeavesByRecipeId(Long recipeId);

    /**
     * Get sick leaves by status
     */
    List<SickLeave> getSickLeavesByStatus(SickLeaveStatus status);

    /**
     * Get active sick leaves for a patient
     */
    List<SickLeave> getActiveSickLeavesByCustomerId(Long customerId);

    /**
     * Get sick leaves in date range
     */
    List<SickLeave> getSickLeavesInDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Count sick leaves by doctor (for statistics)
     */
    List<Object[]> countSickLeavesByDoctor();

    /**
     * Count sick leaves by month (for statistics)
     */
    List<Object[]> countSickLeavesByMonth();

    /**
     * Check if patient has active sick leave on specific date
     */
    Boolean hasActiveSickLeaveOnDate(Long customerId, LocalDate date);

    /**
     * Extend sick leave duration
     */
    SickLeave extendSickLeave(Long id, Integer additionalDays, String reason);

    /**
     * Cancel sick leave
     */
    SickLeave cancelSickLeave(Long id, String reason);

    /**
     * Mark sick leave as completed
     */
    SickLeave completeSickLeave(Long id);

    /**
     * Generate unique sick leave number
     */
    String generateLeaveNumber();
}
