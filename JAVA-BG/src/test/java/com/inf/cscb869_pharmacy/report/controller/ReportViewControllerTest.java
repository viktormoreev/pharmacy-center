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
        List<MonthlyStatisticsDTO> statistics = List.of(
                MonthlyStatisticsDTO.builder().year(2026).month(1).monthName("JANUARY").count(2L).build(),
                MonthlyStatisticsDTO.builder().year(2026).month(2).monthName("FEBRUARY").count(8L).build()
        );
        when(reportService.getSickLeavesByMonth()).thenReturn(statistics);

        Model model = new ExtendedModelMap();
        String view = controller.sickLeavesByMonth(model);

        assertThat(view).isEqualTo("reports/sick-leaves-by-month");
        assertThat(model.getAttribute("statistics")).isEqualTo(statistics);
        assertThat(model.getAttribute("maxCount")).isEqualTo(8L);
    }

    @Test
    void sickLeavesByMonthShouldSetZeroMaxCountForEmptyStatistics() {
        when(reportService.getSickLeavesByMonth()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.sickLeavesByMonth(model);

        assertThat(view).isEqualTo("reports/sick-leaves-by-month");
        assertThat(model.getAttribute("maxCount")).isEqualTo(0L);
    }
}
