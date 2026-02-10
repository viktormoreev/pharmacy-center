package com.inf.cscb869_pharmacy.report.service.impl;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.repository.CustomerRepository;
import com.inf.cscb869_pharmacy.doctor.repository.DoctorRepository;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.repository.RecipeRepository;
import com.inf.cscb869_pharmacy.report.dto.DiagnosisReportDTO;
import com.inf.cscb869_pharmacy.report.dto.DoctorStatisticsDTO;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ReportService for generating medical reports and statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CustomerRepository customerRepository;
    private final DoctorRepository doctorRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public List<Customer> getPatientsByDiagnosis(String diagnosis) {
        log.info("Finding patients with diagnosis: {}", diagnosis);
        
        List<Recipe> recipes = recipeRepository.findByDiagnosisContainingIgnoreCase(diagnosis);
        
        // Extract unique customers from recipes
        return recipes.stream()
                .map(Recipe::getCustomer)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public long countPatientsByDiagnosis(String diagnosis) {
        log.info("Counting patients with diagnosis: {}", diagnosis);
        return recipeRepository.countDistinctPatientsByDiagnosis(diagnosis);
    }

    @Override
    public List<DiagnosisReportDTO> getMostCommonDiagnoses() {
        log.info("Fetching most common diagnoses");
        
        List<Object[]> results = recipeRepository.findMostCommonDiagnoses();
        
        return results.stream()
                .map(row -> DiagnosisReportDTO.builder()
                        .diagnosis((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> getPatientsByPrimaryDoctor(Long doctorId) {
        log.info("Finding patients for primary doctor ID: {}", doctorId);
        return customerRepository.findByPrimaryDoctorId(doctorId);
    }

    @Override
    public List<DoctorStatisticsDTO> getPatientCountPerPrimaryDoctor() {
        log.info("Fetching patient count per primary doctor");
        
        List<Object[]> results = doctorRepository.countPatientsByPrimaryDoctor();
        
        return results.stream()
                .map(row -> DoctorStatisticsDTO.builder()
                        .doctorName((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorStatisticsDTO> getVisitCountPerDoctor() {
        log.info("Fetching visit count per doctor");
        
        List<Object[]> results = doctorRepository.countVisitsByDoctor();
        
        return results.stream()
                .map(row -> DoctorStatisticsDTO.builder()
                        .doctorName((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> getPatientMedicalHistory(Long customerId) {
        log.info("Fetching medical history for customer ID: {}", customerId);
        return recipeRepository.findByCustomerIdOrderByCreationDateDesc(customerId);
    }

    @Override
    public List<Recipe> getExaminationsInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching examinations between {} and {}", startDate, endDate);
        return recipeRepository.findByCreationDateBetween(startDate, endDate);
    }

    @Override
    public List<Recipe> getDoctorExaminationsInDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching examinations for doctor ID {} between {} and {}", doctorId, startDate, endDate);
        return recipeRepository.findByDoctorIdAndCreationDateBetween(doctorId, startDate, endDate);
    }

    @Override
    public List<MonthlyStatisticsDTO> getSickLeavesByMonth() {
        log.info("Fetching sick leaves grouped by month");
        
        List<Object[]> results = recipeRepository.countSickLeavesByMonth();
        
        return results.stream()
                .map(row -> {
                    // DB dialects may return different numeric types (Integer, Long, BigInteger, etc.)
                    Integer year = row[0] instanceof Number ? ((Number) row[0]).intValue() : null;
                    Integer month = row[1] instanceof Number ? ((Number) row[1]).intValue() : null;
                    Long count = row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L;
                    
                    return MonthlyStatisticsDTO.builder()
                            .year(year)
                            .month(month)
                            .monthName((month != null && month >= 1 && month <= 12) ? Month.of(month).name() : "UNKNOWN")
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorStatisticsDTO> getDoctorsSickLeaveRanking() {
        log.info("Fetching doctors ranked by sick leaves issued");
        
        List<Object[]> results = recipeRepository.countSickLeavesByDoctor();
        
        return results.stream()
                .map(row -> DoctorStatisticsDTO.builder()
                        .doctorName((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> getCustomersWithValidInsurance() {
        log.info("Fetching customers with valid insurance");
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return customerRepository.findWithValidInsurance(sixMonthsAgo);
    }

    @Override
    public List<Customer> getCustomersWithoutValidInsurance() {
        log.info("Fetching customers without valid insurance");
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return customerRepository.findWithoutValidInsurance(sixMonthsAgo);
    }
}
