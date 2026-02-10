package com.inf.cscb869_pharmacy.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for diagnosis report statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisReportDTO {
    private String diagnosis;
    private Long count;
}
