package com.inf.cscb869_pharmacy.report.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.report.dto.DiagnosisReportDTO;
import com.inf.cscb869_pharmacy.report.dto.DoctorStatisticsDTO;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for generating medical reports and statistics
 * Справки и отчети за медицинската система
 */
public interface ReportService {

    /**
     * a) Get list of patients with a specific diagnosis
     * Списък с пациенти с дадена диагноза
     */
    List<Customer> getPatientsByDiagnosis(String diagnosis);

    /**
     * Count unique patients with a specific diagnosis
     */
    long countPatientsByDiagnosis(String diagnosis);

    /**
     * b) Get most common diagnoses with counts
     * Най-често срещани диагнози
     */
    List<DiagnosisReportDTO> getMostCommonDiagnoses();

    /**
     * c) Get patients registered with a specific primary doctor
     * Пациенти при даден личен лекар
     */
    List<Customer> getPatientsByPrimaryDoctor(Long doctorId);

    /**
     * d) Get count of patients per primary doctor
     * Брой пациенти при всеки личен лекар
     */
    List<DoctorStatisticsDTO> getPatientCountPerPrimaryDoctor();

    /**
     * e) Get count of visits (examinations) per doctor
     * Брой посещения при всеки лекар
     */
    List<DoctorStatisticsDTO> getVisitCountPerDoctor();

    /**
     * f) Get patient's complete medical history
     * История на пациент (всички прегледи)
     */
    List<Recipe> getPatientMedicalHistory(Long customerId);

    /**
     * g) Get all examinations within a date range
     * Всички прегледи в период
     */
    List<Recipe> getExaminationsInDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * h) Get doctor's examinations within a date range
     * Прегледи при даден лекар в период
     */
    List<Recipe> getDoctorExaminationsInDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * i) Get sick leaves grouped by month
     * Болнични по месеци
     */
    List<MonthlyStatisticsDTO> getSickLeavesByMonth();

    /**
     * j) Get doctors ranked by number of sick leaves issued
     * Лекари с най-много издадени болнични
     */
    List<DoctorStatisticsDTO> getDoctorsSickLeaveRanking();

    /**
     * Get customers with valid insurance
     */
    List<Customer> getCustomersWithValidInsurance();

    /**
     * Get customers without valid insurance
     */
    List<Customer> getCustomersWithoutValidInsurance();
}
