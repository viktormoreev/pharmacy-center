package com.inf.cscb869_pharmacy.report.controller;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.report.dto.DiagnosisReportDTO;
import com.inf.cscb869_pharmacy.report.dto.DoctorStatisticsDTO;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API Controller for Medical Reports and Statistics
 * Справки и отчети за медицинската система
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportApiController {

    private final ReportService reportService;

    /**
     * a) Get patients with a specific diagnosis
     * GET /api/reports/patients-by-diagnosis?diagnosis=грип
     */
    @GetMapping("/patients-by-diagnosis")
    public ResponseEntity<List<Customer>> getPatientsByDiagnosis(
            @RequestParam String diagnosis) {
        log.info("API: Getting patients with diagnosis: {}", diagnosis);
        return ResponseEntity.ok(reportService.getPatientsByDiagnosis(diagnosis));
    }

    /**
     * b) Get most common diagnoses
     * GET /api/reports/common-diagnoses
     */
    @GetMapping("/common-diagnoses")
    public ResponseEntity<List<DiagnosisReportDTO>> getCommonDiagnoses() {
        log.info("API: Getting common diagnoses");
        return ResponseEntity.ok(reportService.getMostCommonDiagnoses());
    }

    /**
     * c) Get patients by primary doctor
     * GET /api/reports/patients-by-primary-doctor/1
     */
    @GetMapping("/patients-by-primary-doctor/{doctorId}")
    public ResponseEntity<List<Customer>> getPatientsByPrimaryDoctor(
            @PathVariable Long doctorId) {
        log.info("API: Getting patients for primary doctor ID: {}", doctorId);
        return ResponseEntity.ok(reportService.getPatientsByPrimaryDoctor(doctorId));
    }

    /**
     * d) Get patient count per primary doctor
     * GET /api/reports/patient-count-by-primary-doctor
     */
    @GetMapping("/patient-count-by-primary-doctor")
    public ResponseEntity<List<DoctorStatisticsDTO>> getPatientCountByPrimaryDoctor() {
        log.info("API: Getting patient count by primary doctor");
        return ResponseEntity.ok(reportService.getPatientCountPerPrimaryDoctor());
    }

    /**
     * e) Get visit count per doctor
     * GET /api/reports/visit-count-by-doctor
     */
    @GetMapping("/visit-count-by-doctor")
    public ResponseEntity<List<DoctorStatisticsDTO>> getVisitCountByDoctor() {
        log.info("API: Getting visit count by doctor");
        return ResponseEntity.ok(reportService.getVisitCountPerDoctor());
    }

    /**
     * f) Get patient medical history
     * GET /api/reports/patient-history/1
     */
    @GetMapping("/patient-history/{customerId}")
    public ResponseEntity<List<Recipe>> getPatientHistory(
            @PathVariable Long customerId) {
        log.info("API: Getting medical history for customer ID: {}", customerId);
        return ResponseEntity.ok(reportService.getPatientMedicalHistory(customerId));
    }

    /**
     * g) Get examinations in date range
     * GET /api/reports/examinations?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/examinations")
    public ResponseEntity<List<Recipe>> getExaminationsInDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("API: Getting examinations between {} and {}", startDate, endDate);
        return ResponseEntity.ok(reportService.getExaminationsInDateRange(startDate, endDate));
    }

    /**
     * h) Get doctor's examinations in date range
     * GET /api/reports/doctor-examinations/1?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/doctor-examinations/{doctorId}")
    public ResponseEntity<List<Recipe>> getDoctorExaminationsInDateRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("API: Getting examinations for doctor {} between {} and {}", 
                doctorId, startDate, endDate);
        return ResponseEntity.ok(
                reportService.getDoctorExaminationsInDateRange(doctorId, startDate, endDate));
    }

    /**
     * i) Get sick leaves by month
     * GET /api/reports/sick-leaves-by-month
     */
    @GetMapping("/sick-leaves-by-month")
    public ResponseEntity<List<MonthlyStatisticsDTO>> getSickLeavesByMonth() {
        log.info("API: Getting sick leaves by month");
        return ResponseEntity.ok(reportService.getSickLeavesByMonth());
    }

    /**
     * j) Get doctors ranked by sick leaves issued
     * GET /api/reports/doctors-sick-leave-ranking
     */
    @GetMapping("/doctors-sick-leave-ranking")
    public ResponseEntity<List<DoctorStatisticsDTO>> getDoctorsSickLeaveRanking() {
        log.info("API: Getting doctors sick leave ranking");
        return ResponseEntity.ok(reportService.getDoctorsSickLeaveRanking());
    }

    /**
     * Get customers with valid insurance
     * GET /api/reports/valid-insurance
     */
    @GetMapping("/valid-insurance")
    public ResponseEntity<List<Customer>> getCustomersWithValidInsurance() {
        log.info("API: Getting customers with valid insurance");
        return ResponseEntity.ok(reportService.getCustomersWithValidInsurance());
    }

    /**
     * Get customers without valid insurance
     * GET /api/reports/invalid-insurance
     */
    @GetMapping("/invalid-insurance")
    public ResponseEntity<List<Customer>> getCustomersWithoutValidInsurance() {
        log.info("API: Getting customers without valid insurance");
        return ResponseEntity.ok(reportService.getCustomersWithoutValidInsurance());
    }
}
