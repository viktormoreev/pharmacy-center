package com.inf.cscb869_pharmacy.sickleave.service;

import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;

import java.time.LocalDate;
import java.util.List;

public interface SickLeaveService {

    SickLeave createSickLeave(SickLeave sickLeave);

    SickLeave updateSickLeave(Long id, SickLeave sickLeave);

    void deleteSickLeave(Long id);

    SickLeave getSickLeaveById(Long id);

    List<SickLeave> getAllSickLeaves();

    SickLeave getSickLeaveByNumber(String leaveNumber);

    List<SickLeave> getSickLeavesByCustomerId(Long customerId);

    List<SickLeave> getSickLeavesByDoctorId(Long doctorId);

    List<SickLeave> getSickLeavesByRecipeId(Long recipeId);

    List<SickLeave> getSickLeavesByStatus(SickLeaveStatus status);

    List<SickLeave> getActiveSickLeavesByCustomerId(Long customerId);

    List<SickLeave> getSickLeavesInDateRange(LocalDate startDate, LocalDate endDate);

    List<Object[]> countSickLeavesByDoctor();

    List<Object[]> countSickLeavesByMonth();

    Boolean hasActiveSickLeaveOnDate(Long customerId, LocalDate date);

    SickLeave extendSickLeave(Long id, Integer additionalDays, String reason);

    SickLeave cancelSickLeave(Long id, String reason);

    SickLeave completeSickLeave(Long id);

    String generateLeaveNumber();
}
