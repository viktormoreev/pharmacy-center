package com.inf.cscb869_pharmacy.recipe.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe extends BaseEntity {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "The date cannot be in the future!")
    @NotNull(message = "Creation date is required")
    @Column(nullable = false)
    private LocalDate creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecipeStatus status = RecipeStatus.ACTIVE;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 2000)
    private String notes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Column(name = "sick_leave", nullable = false)
    @Builder.Default
    private Boolean sickLeave = false;

    @Column(name = "sick_leave_days")
    private Integer sickLeaveDays;

    @Column(name = "sick_leave_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sickLeaveStartDate;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeMedicine> recipeMedicines = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SickLeave> sickLeaves = new ArrayList<>();

    public void addMedicine(RecipeMedicine recipeMedicine) {
        recipeMedicines.add(recipeMedicine);
        recipeMedicine.setRecipe(this);
    }

    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expirationDate);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + getId() +
                ", creationDate=" + creationDate +
                ", status=" + status +
                ", doctor=" + (doctor != null ? doctor.getName() : "null") +
                ", customer=" + (customer != null ? customer.getName() : "null") +
                ", diagnosis='" + diagnosis + '\'' +
                ", sickLeave=" + sickLeave +
                ", medicinesCount=" + recipeMedicines.size() +
                '}';
    }
}
