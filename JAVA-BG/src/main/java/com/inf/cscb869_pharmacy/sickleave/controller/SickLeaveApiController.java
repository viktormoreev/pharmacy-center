package com.inf.cscb869_pharmacy.sickleave.controller;

import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import com.inf.cscb869_pharmacy.sickleave.service.SickLeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Sick Leave management
 * REST API контролер за управление на болнични листове
 */
@RestController
@RequestMapping("/api/sick-leaves")
@RequiredArgsConstructor
@Slf4j
public class SickLeaveApiController {

    private final SickLeaveService sickLeaveService;

    @GetMapping
    public ResponseEntity<List<SickLeave>> getAllSickLeaves() {
        log.info("API: Getting all sick leaves");
        return ResponseEntity.ok(sickLeaveService.getAllSickLeaves());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SickLeave> getSickLeaveById(@PathVariable Long id) {
        log.info("API: Getting sick leave by ID: {}", id);
        return ResponseEntity.ok(sickLeaveService.getSickLeaveById(id));
    }

    @GetMapping("/number/{leaveNumber}")
    public ResponseEntity<SickLeave> getSickLeaveByNumber(@PathVariable String leaveNumber) {
        log.info("API: Getting sick leave by number: {}", leaveNumber);
        return ResponseEntity.ok(sickLeaveService.getSickLeaveByNumber(leaveNumber));
    }

    @PostMapping
    public ResponseEntity<SickLeave> createSickLeave(@RequestBody SickLeave sickLeave) {
        log.info("API: Creating new sick leave");
        return ResponseEntity.ok(sickLeaveService.createSickLeave(sickLeave));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SickLeave> updateSickLeave(
            @PathVariable Long id,
            @RequestBody SickLeave sickLeave) {
        log.info("API: Updating sick leave with ID: {}", id);
        return ResponseEntity.ok(sickLeaveService.updateSickLeave(id, sickLeave));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSickLeave(@PathVariable Long id) {
        log.info("API: Deleting sick leave with ID: {}", id);
        sickLeaveService.deleteSickLeave(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SickLeave>> getSickLeavesByCustomerId(@PathVariable Long customerId) {
        log.info("API: Getting sick leaves for customer ID: {}", customerId);
        return ResponseEntity.ok(sickLeaveService.getSickLeavesByCustomerId(customerId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<SickLeave>> getSickLeavesByDoctorId(@PathVariable Long doctorId) {
        log.info("API: Getting sick leaves for doctor ID: {}", doctorId);
        return ResponseEntity.ok(sickLeaveService.getSickLeavesByDoctorId(doctorId));
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<SickLeave>> getSickLeavesByRecipeId(@PathVariable Long recipeId) {
        log.info("API: Getting sick leaves for recipe ID: {}", recipeId);
        return ResponseEntity.ok(sickLeaveService.getSickLeavesByRecipeId(recipeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SickLeave>> getSickLeavesByStatus(@PathVariable SickLeaveStatus status) {
        log.info("API: Getting sick leaves with status: {}", status);
        return ResponseEntity.ok(sickLeaveService.getSickLeavesByStatus(status));
    }

    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<SickLeave>> getActiveSickLeavesByCustomerId(@PathVariable Long customerId) {
        log.info("API: Getting active sick leaves for customer ID: {}", customerId);
        return ResponseEntity.ok(sickLeaveService.getActiveSickLeavesByCustomerId(customerId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SickLeave>> getSickLeavesInDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("API: Getting sick leaves between {} and {}", startDate, endDate);
        return ResponseEntity.ok(sickLeaveService.getSickLeavesInDateRange(startDate, endDate));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<SickLeave> extendSickLeave(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("API: Extending sick leave {}", id);
        
        Integer additionalDays = (Integer) request.get("additionalDays");
        String reason = (String) request.get("reason");
        
        return ResponseEntity.ok(sickLeaveService.extendSickLeave(id, additionalDays, reason));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SickLeave> cancelSickLeave(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("API: Cancelling sick leave {}", id);
        
        String reason = request.get("reason");
        return ResponseEntity.ok(sickLeaveService.cancelSickLeave(id, reason));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SickLeave> completeSickLeave(@PathVariable Long id) {
        log.info("API: Completing sick leave {}", id);
        return ResponseEntity.ok(sickLeaveService.completeSickLeave(id));
    }

    @GetMapping("/customer/{customerId}/check")
    public ResponseEntity<Boolean> hasActiveSickLeaveOnDate(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("API: Checking if customer {} has active sick leave on {}", customerId, date);
        return ResponseEntity.ok(sickLeaveService.hasActiveSickLeaveOnDate(customerId, date));
    }

    @GetMapping("/statistics/by-doctor")
    public ResponseEntity<List<Object[]>> countSickLeavesByDoctor() {
        log.info("API: Getting sick leave statistics by doctor");
        return ResponseEntity.ok(sickLeaveService.countSickLeavesByDoctor());
    }

    @GetMapping("/statistics/by-month")
    public ResponseEntity<List<Object[]>> countSickLeavesByMonth() {
        log.info("API: Getting sick leave statistics by month");
        return ResponseEntity.ok(sickLeaveService.countSickLeavesByMonth());
    }

    @GetMapping("/generate-number")
    public ResponseEntity<Map<String, String>> generateLeaveNumber() {
        String leaveNumber = sickLeaveService.generateLeaveNumber();
        return ResponseEntity.ok(Map.of("leaveNumber", leaveNumber));
    }
}
