package com.inf.cscb869_pharmacy.recipe.dto;

import com.inf.cscb869_pharmacy.recipe.entity.RecipeStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {

    private Long id;

    @NotNull(message = "Creation date is required")
    @PastOrPresent(message = "Creation date cannot be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate;

    @NotNull(message = "Doctor is required")
    private Long doctorId;

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotNull(message = "Status is required")
    private RecipeStatus status;

    @Builder.Default
    private List<String> selectedDiagnoses = new ArrayList<>();

    private String notes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Valid
    @Builder.Default
    private List<RecipeMedicineDTO> medicines = new ArrayList<>();

    public void addMedicine(RecipeMedicineDTO medicine) {
        if (medicines == null) {
            medicines = new ArrayList<>();
        }
        medicines.add(medicine);
    }

    public void removeMedicine(int index) {
        if (medicines != null && index >= 0 && index < medicines.size()) {
            medicines.remove(index);
        }
    }
}
