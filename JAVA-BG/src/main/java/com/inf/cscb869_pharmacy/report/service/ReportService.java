package com.inf.cscb869_pharmacy.report.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.report.dto.DiagnosisReportDTO;
import com.inf.cscb869_pharmacy.report.dto.DoctorStatisticsDTO;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<Customer> getPatientsByDiagnosis(String diagnosis);

    long countPatientsByDiagnosis(String diagnosis);

    List<DiagnosisReportDTO> getMostCommonDiagnoses();

    List<Customer> getPatientsByPrimaryDoctor(Long doctorId);

    List<DoctorStatisticsDTO> getPatientCountPerPrimaryDoctor();

    List<DoctorStatisticsDTO> getVisitCountPerDoctor();

    List<Recipe> getPatientMedicalHistory(Long customerId);

    List<Recipe> getExaminationsInDateRange(LocalDate startDate, LocalDate endDate);

    List<Recipe> getDoctorExaminationsInDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);

    List<MonthlyStatisticsDTO> getSickLeavesByMonth();

    List<DoctorStatisticsDTO> getDoctorsSickLeaveRanking();

    List<Customer> getCustomersWithValidInsurance();

    List<Customer> getCustomersWithoutValidInsurance();
}
