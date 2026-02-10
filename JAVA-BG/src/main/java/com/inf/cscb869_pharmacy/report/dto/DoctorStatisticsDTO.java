package com.inf.cscb869_pharmacy.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for doctor statistics (visits, patients, sick leaves)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatisticsDTO {
    private String doctorName;
    private Long count;
}
