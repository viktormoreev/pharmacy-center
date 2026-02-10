package com.inf.cscb869_pharmacy.diagnosis.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Diagnosis entity - represents a medical diagnosis made by a doctor
 * Диагноза поставена от лекар при преглед
 */
@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis extends BaseEntity {

    /**
     * The recipe/examination where this diagnosis was made
     * Прегледът, при който е поставена диагнозата
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * ICD-10 code (optional)
     * Код по МКБ-10
     */
    @Column(name = "icd10_code", length = 10)
    private String icd10Code;

    /**
     * Diagnosis name/description
     * Наименование на диагнозата
     */
    @NotBlank(message = "Diagnosis name is required")
    @Size(min = 3, max = 500, message = "Diagnosis name must be between 3 and 500 characters")
    @Column(nullable = false, length = 500)
    private String name;

    /**
     * Detailed description
     * Подробно описание
     */
    @Column(length = 2000)
    private String description;

    /**
     * Date when diagnosis was made
     * Дата на поставяне на диагнозата
     */
    @Column(name = "diagnosis_date", nullable = false)
    private LocalDate diagnosisDate;

    /**
     * Is this the primary diagnosis for this examination?
     * Основна ли е тази диагноза
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    /**
     * Severity level (MILD, MODERATE, SEVERE, CRITICAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiagnosisSeverity severity;

    /**
     * Additional notes about the diagnosis
     */
    @Column(length = 1000)
    private String notes;

    @Override
    public String toString() {
        return "Diagnosis{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", icd10Code='" + icd10Code + '\'' +
                ", diagnosisDate=" + diagnosisDate +
                ", isPrimary=" + isPrimary +
                '}';
    }
}
