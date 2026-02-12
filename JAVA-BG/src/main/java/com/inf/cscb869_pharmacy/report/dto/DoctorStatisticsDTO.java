package com.inf.cscb869_pharmacy.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatisticsDTO {
    private String doctorName;
    private Long count;
}
