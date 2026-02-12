package com.inf.cscb869_pharmacy.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatisticsDTO {
    private Integer year;
    private Integer month;
    private String monthName;
    private Long count;
}
