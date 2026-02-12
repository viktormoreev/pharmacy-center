package com.inf.cscb869_pharmacy.report.service.impl;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.repository.CustomerRepository;
import com.inf.cscb869_pharmacy.doctor.repository.DoctorRepository;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.repository.RecipeRepository;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void getPatientsByDiagnosisShouldReturnDistinctCustomers() {
        Customer first = Customer.builder().name("Alice").egn("1234567890").age(30).build();
        Customer second = Customer.builder().name("Bob").egn("1234567891").age(28).build();

        Recipe r1 = Recipe.builder().customer(first).build();
        Recipe r2 = Recipe.builder().customer(first).build();
        Recipe r3 = Recipe.builder().customer(second).build();

        when(recipeRepository.findByDiagnosisContainingIgnoreCase("flu"))
                .thenReturn(List.of(r1, r2, r3));

        List<Customer> result = reportService.getPatientsByDiagnosis("flu");

        assertThat(result).containsExactly(first, second);
    }

    @Test
    void getSickLeavesByMonthShouldMapNumericTypesAndMonthName() {
        when(recipeRepository.countSickLeavesByMonth()).thenReturn(List.of(
                new Object[]{2026, 2, 4L},
                new Object[]{2025L, 13, 1}
        ));

        List<MonthlyStatisticsDTO> result = reportService.getSickLeavesByMonth();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getYear()).isEqualTo(2026);
        assertThat(result.get(0).getMonth()).isEqualTo(2);
        assertThat(result.get(0).getMonthName()).isEqualTo("FEBRUARY");
        assertThat(result.get(0).getCount()).isEqualTo(4L);

        assertThat(result.get(1).getYear()).isEqualTo(2025);
        assertThat(result.get(1).getMonth()).isEqualTo(13);
        assertThat(result.get(1).getMonthName()).isEqualTo("UNKNOWN");
        assertThat(result.get(1).getCount()).isEqualTo(1L);
    }

    @Test
    void getCustomersWithValidInsuranceShouldUseTodayBoundary() {
        when(customerRepository.findWithValidInsurance(org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of());

        reportService.getCustomersWithValidInsurance();

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(customerRepository).findWithValidInsurance(captor.capture());

        assertThat(captor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void getCustomersWithoutValidInsuranceShouldUseTodayBoundary() {
        when(customerRepository.findWithoutValidInsurance(org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of());

        reportService.getCustomersWithoutValidInsurance();

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(customerRepository).findWithoutValidInsurance(captor.capture());

        assertThat(captor.getValue()).isEqualTo(LocalDate.now());
    }
}
