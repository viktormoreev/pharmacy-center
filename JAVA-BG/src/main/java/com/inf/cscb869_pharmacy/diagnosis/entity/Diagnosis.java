package com.inf.cscb869_pharmacy.diagnosis.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "icd10_code", length = 10)
    private String icd10Code;

    @NotBlank(message = "Diagnosis name is required")
    @Size(min = 3, max = 500, message = "Diagnosis name must be between 3 and 500 characters")
    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "diagnosis_date", nullable = false)
    private LocalDate diagnosisDate;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiagnosisSeverity severity;

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
