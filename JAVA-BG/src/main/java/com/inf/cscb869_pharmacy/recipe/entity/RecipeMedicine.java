package com.inf.cscb869_pharmacy.recipe.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "recipe_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeMedicine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @NotBlank(message = "Dosage is required")
    @Column(nullable = false, length = 500)
    private String dosage;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(nullable = false)
    private Integer durationDays;

    @Column(length = 1000)
    private String instructions;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Override
    public String toString() {
        return "RecipeMedicine{" +
                "id=" + getId() +
                ", medicine=" + (medicine != null ? medicine.getName() : "null") +
                ", dosage='" + dosage + '\'' +
                ", durationDays=" + durationDays +
                ", quantity=" + quantity +
                '}';
    }
}
