package com.inf.cscb869_pharmacy.report.controller;

import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * View Controller for Medical Reports and Statistics
 * Контролер за справки и отчети
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportViewController {

    private final ReportService reportService;
    private final DoctorService doctorService;
    private final CustomerService customerService;

    /**
     * Main reports page
     */
    @GetMapping
    public String reportsIndex() {
        log.info("Displaying reports index page");
        return "reports/index";
    }

    /**
     * Patients by diagnosis report
     */
    @GetMapping("/patients-by-diagnosis")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','ADMIN')")
    public String patientsByDiagnosisForm(Model model) {
        log.info("Displaying patients by diagnosis form");
        return "reports/patients-by-diagnosis";
    }

    @PostMapping("/patients-by-diagnosis")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','ADMIN')")
    public String patientsByDiagnosisReport(@RequestParam String diagnosis, Model model) {
        log.info("Searching patients with diagnosis: {}", diagnosis);
        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("patients", reportService.getPatientsByDiagnosis(diagnosis));
        model.addAttribute("count", reportService.countPatientsByDiagnosis(diagnosis));
        return "reports/patients-by-diagnosis";
    }

    /**
     * Common diagnoses report
     */
    @GetMapping("/common-diagnoses")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','ADMIN')")
    public String commonDiagnoses(Model model) {
        log.info("Displaying common diagnoses report");
        model.addAttribute("diagnoses", reportService.getMostCommonDiagnoses());
        return "reports/common-diagnoses";
    }

    /**
     * Patients by primary doctor
     */
    @GetMapping("/patients-by-primary-doctor")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String patientsByPrimaryDoctorForm(Model model) {
        log.info("Displaying patients by primary doctor form");
        model.addAttribute("doctors", doctorService.getDoctors());
        return "reports/patients-by-primary-doctor";
    }

    @GetMapping("/patients-by-primary-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String patientsByPrimaryDoctorReport(@PathVariable Long doctorId, Model model) {
        log.info("Showing patients for primary doctor ID: {}", doctorId);
        model.addAttribute("doctor", doctorService.getDoctor(doctorId));
        model.addAttribute("patients", reportService.getPatientsByPrimaryDoctor(doctorId));
        model.addAttribute("doctors", doctorService.getDoctors());
        return "reports/patients-by-primary-doctor";
    }

    /**
     * Primary doctor statistics
     */
    @GetMapping("/primary-doctor-statistics")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String primaryDoctorStatistics(Model model) {
        log.info("Displaying primary doctor statistics");
        model.addAttribute("statistics", reportService.getPatientCountPerPrimaryDoctor());
        return "reports/primary-doctor-statistics";
    }

    /**
     * Doctor visit statistics
     */
    @GetMapping("/doctor-visit-statistics")
    public String doctorVisitStatistics(Model model) {
        log.info("Displaying doctor visit statistics");
        model.addAttribute("statistics", reportService.getVisitCountPerDoctor());
        return "reports/doctor-visit-statistics";
    }

    /**
     * Patient medical history
     */
    @GetMapping("/patient-history")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String patientHistoryForm(Model model) {
        log.info("Displaying patient history form");
        model.addAttribute("customers", customerService.getAllCustomers());
        return "reports/patient-history";
    }

    @GetMapping("/patient-history/{customerId}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String patientHistoryReport(@PathVariable Long customerId, Model model) {
        log.info("Showing medical history for customer ID: {}", customerId);
        model.addAttribute("customer", customerService.getCustomerById(customerId));
        model.addAttribute("history", reportService.getPatientMedicalHistory(customerId));
        model.addAttribute("customers", customerService.getAllCustomers());
        return "reports/patient-history";
    }

    /**
     * Examinations report (with date range)
     */
    @GetMapping("/examinations")
    public String examinationsForm(Model model) {
        log.info("Displaying examinations report form");
        model.addAttribute("doctors", doctorService.getDoctors());
        return "reports/examinations";
    }

    @PostMapping("/examinations")
    public String examinationsReport(
            @RequestParam(required = false) Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {
        
        log.info("Searching examinations from {} to {}, doctor: {}", startDate, endDate, doctorId);
        
        if (doctorId != null && doctorId > 0) {
            model.addAttribute("examinations", 
                    reportService.getDoctorExaminationsInDateRange(doctorId, startDate, endDate));
            model.addAttribute("selectedDoctor", doctorService.getDoctor(doctorId));
        } else {
            model.addAttribute("examinations", 
                    reportService.getExaminationsInDateRange(startDate, endDate));
        }
        
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("doctors", doctorService.getDoctors());
        return "reports/examinations";
    }

    /**
     * Sick leaves by month
     */
    @GetMapping("/sick-leaves-by-month")
    public String sickLeavesByMonth(Model model) {
        log.info("Displaying sick leaves by month");
        List<MonthlyStatisticsDTO> statistics = reportService.getSickLeavesByMonth();
        long maxCount = statistics.stream()
                .mapToLong(stat -> stat.getCount() != null ? stat.getCount() : 0L)
                .max()
                .orElse(0L);
        model.addAttribute("statistics", statistics);
        model.addAttribute("maxCount", maxCount);
        return "reports/sick-leaves-by-month";
    }

    /**
     * Doctors sick leave ranking
     */
    @GetMapping("/doctors-sick-leave-ranking")
    public String doctorsSickLeaveRanking(Model model) {
        log.info("Displaying doctors sick leave ranking");
        model.addAttribute("ranking", reportService.getDoctorsSickLeaveRanking());
        return "reports/doctors-sick-leave-ranking";
    }

    /**
     * Insurance status report
     */
    @GetMapping("/insurance-status")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String insuranceStatus(Model model) {
        log.info("Displaying insurance status report");
        model.addAttribute("validInsurance", reportService.getCustomersWithValidInsurance());
        model.addAttribute("invalidInsurance", reportService.getCustomersWithoutValidInsurance());
        return "reports/insurance-status";
    }
}
