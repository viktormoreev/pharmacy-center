package com.inf.cscb869_pharmacy.report.controller;

import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.report.dto.MonthlyStatisticsDTO;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportViewControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ReportViewController controller;

    @Test
    void sickLeavesByMonthShouldPopulateStatisticsAndMaxCount() {
        // Arrange: non-empty monthly stats.
        List<MonthlyStatisticsDTO> statistics = List.of(
                MonthlyStatisticsDTO.builder().year(2026).month(1).monthName("JANUARY").count(2L).build(),
                MonthlyStatisticsDTO.builder().year(2026).month(2).monthName("FEBRUARY").count(8L).build()
        );
        when(reportService.getSickLeavesByMonth()).thenReturn(statistics);

        // Act
        Model model = new ExtendedModelMap();
        String view = controller.sickLeavesByMonth(model);

        // Assert: model contains original data and computed maxCount.
        assertThat(view).isEqualTo("reports/sick-leaves-by-month");
        assertThat(model.getAttribute("statistics")).isEqualTo(statistics);
        assertThat(model.getAttribute("maxCount")).isEqualTo(8L);
    }

    @Test
    void sickLeavesByMonthShouldSetZeroMaxCountForEmptyStatistics() {
        // Arrange: empty response from service.
        when(reportService.getSickLeavesByMonth()).thenReturn(List.of());

        // Act
        Model model = new ExtendedModelMap();
        String view = controller.sickLeavesByMonth(model);

        // Assert: template still renders with zero maxCount.
        assertThat(view).isEqualTo("reports/sick-leaves-by-month");
        assertThat(model.getAttribute("maxCount")).isEqualTo(0L);
    }
}
