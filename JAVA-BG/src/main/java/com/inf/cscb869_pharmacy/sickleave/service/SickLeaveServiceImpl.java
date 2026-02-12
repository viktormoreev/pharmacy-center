package com.inf.cscb869_pharmacy.sickleave.service;

import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import com.inf.cscb869_pharmacy.sickleave.repository.SickLeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Sick Leave management
 * Имплементация на услуги за управление на болнични листове
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SickLeaveServiceImpl implements SickLeaveService {

    private final SickLeaveRepository sickLeaveRepository;

    @Override
    public SickLeave createSickLeave(SickLeave sickLeave) {
        Long recipeId = sickLeave.getRecipe() != null ? sickLeave.getRecipe().getId() : null;
        log.info("Creating new sick leave for recipe ID: {}", recipeId);

        if (sickLeave.getLeaveNumber() == null || sickLeave.getLeaveNumber().isEmpty()) {
            sickLeave.setLeaveNumber(generateLeaveNumber());
        }

        if (sickLeave.getIssueDate() == null) {
            sickLeave.setIssueDate(LocalDate.now());
        }

        if (sickLeave.getStatus() == null) {
            sickLeave.setStatus(SickLeaveStatus.ACTIVE);
        }

        return sickLeaveRepository.save(sickLeave);
    }

    @Override
    public SickLeave updateSickLeave(Long id, SickLeave sickLeave) {
        log.info("Updating sick leave with ID: {}", id);
        SickLeave existing = getSickLeaveById(id);

        existing.setStartDate(sickLeave.getStartDate());
        existing.setDurationDays(sickLeave.getDurationDays());
        existing.setReason(sickLeave.getReason());
        existing.setStatus(sickLeave.getStatus());
        existing.setNotes(sickLeave.getNotes());

        return sickLeaveRepository.save(existing);
    }

    @Override
    public void deleteSickLeave(Long id) {
        log.info("Deleting sick leave with ID: {}", id);
        sickLeaveRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SickLeave getSickLeaveById(Long id) {
        return sickLeaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sick leave not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getAllSickLeaves() {
        log.info("Fetching all sick leaves");
        return sickLeaveRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public SickLeave getSickLeaveByNumber(String leaveNumber) {
        log.info("Fetching sick leave by number: {}", leaveNumber);
        return sickLeaveRepository.findByLeaveNumber(leaveNumber)
                .orElseThrow(() -> new RuntimeException("Sick leave not found with number: " + leaveNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getSickLeavesByCustomerId(Long customerId) {
        log.info("Fetching sick leaves for customer ID: {}", customerId);
        return sickLeaveRepository.findByRecipeCustomerIdOrderByStartDateDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getSickLeavesByDoctorId(Long doctorId) {
        log.info("Fetching sick leaves for doctor ID: {}", doctorId);
        return sickLeaveRepository.findByRecipeDoctorIdOrderByIssueDateDesc(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getSickLeavesByRecipeId(Long recipeId) {
        log.info("Fetching sick leaves for recipe ID: {}", recipeId);
        return sickLeaveRepository.findByRecipeId(recipeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getSickLeavesByStatus(SickLeaveStatus status) {
        log.info("Fetching sick leaves with status: {}", status);
        return sickLeaveRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getActiveSickLeavesByCustomerId(Long customerId) {
        log.info("Fetching active sick leaves for customer ID: {}", customerId);
        return sickLeaveRepository.findActiveSickLeavesByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeave> getSickLeavesInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching sick leaves between {} and {}", startDate, endDate);
        return sickLeaveRepository.findByStartDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countSickLeavesByDoctor() {
        log.info("Counting sick leaves by doctor");
        return sickLeaveRepository.countSickLeavesByDoctor();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countSickLeavesByMonth() {
        log.info("Counting sick leaves by month");
        return sickLeaveRepository.countSickLeavesByMonth();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasActiveSickLeaveOnDate(Long customerId, LocalDate date) {
        log.info("Checking if customer {} has active sick leave on {}", customerId, date);
        return sickLeaveRepository.hasActiveSickLeaveOnDate(customerId, date);
    }

    @Override
    public SickLeave extendSickLeave(Long id, Integer additionalDays, String reason) {
        log.info("Extending sick leave {} by {} days", id, additionalDays);
        SickLeave sickLeave = getSickLeaveById(id);

        sickLeave.setDurationDays(sickLeave.getDurationDays() + additionalDays);
        sickLeave.setStatus(SickLeaveStatus.EXTENDED);

        String extendNote = String.format("Extended by %d days on %s. Reason: %s",
                additionalDays, LocalDate.now(), reason);

        if (sickLeave.getNotes() != null && !sickLeave.getNotes().isEmpty()) {
            sickLeave.setNotes(sickLeave.getNotes() + "\n" + extendNote);
        } else {
            sickLeave.setNotes(extendNote);
        }

        return sickLeaveRepository.save(sickLeave);
    }

    @Override
    public SickLeave cancelSickLeave(Long id, String reason) {
        log.info("Cancelling sick leave {}", id);
        SickLeave sickLeave = getSickLeaveById(id);

        sickLeave.setStatus(SickLeaveStatus.CANCELLED);

        String cancelNote = String.format("Cancelled on %s. Reason: %s", LocalDate.now(), reason);

        if (sickLeave.getNotes() != null && !sickLeave.getNotes().isEmpty()) {
            sickLeave.setNotes(sickLeave.getNotes() + "\n" + cancelNote);
        } else {
            sickLeave.setNotes(cancelNote);
        }

        return sickLeaveRepository.save(sickLeave);
    }

    @Override
    public SickLeave completeSickLeave(Long id) {
        log.info("Marking sick leave {} as completed", id);
        SickLeave sickLeave = getSickLeaveById(id);

        sickLeave.setStatus(SickLeaveStatus.COMPLETED);

        return sickLeaveRepository.save(sickLeave);
    }

    @Override
    public String generateLeaveNumber() {
        // Format: SL-YYYYMMDD-XXXX (e.g., SL-20260126-A1B2)
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String leaveNumber = "SL-" + datePrefix + "-" + uniquePart;

        log.info("Generated sick leave number: {}", leaveNumber);
        return leaveNumber;
    }
}
