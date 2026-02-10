package com.inf.cscb869_pharmacy.recipe.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Recipe/Prescription entity representing a medical examination and prescription.
 * Contains diagnosis, medicines, dosage, treatment information, and sick leave data.
 * Представлява посещение при лекар с диагноза, лечение и евентуално болничен.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe extends BaseEntity {

    /**
     * Date of examination/visit (дата на посещение при лекар)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "The date cannot be in the future!")
    @NotNull(message = "Creation date is required")
    @Column(nullable = false)
    private LocalDate creationDate;

    /**
     * Doctor who examined the patient (лекар, който е прегледал пациента)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    /**
     * Patient who was examined (пациент)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    /**
     * Status of the prescription
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecipeStatus status = RecipeStatus.ACTIVE;

    /**
     * Diagnosis given by the doctor (поставена диагноза)
     */
    @Column(length = 1000)
    private String diagnosis;

    /**
     * Additional notes from the doctor
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Expiration date of the prescription
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    /**
     * Sick leave issued (дали е издаден болничен лист)
     */
    @Column(name = "sick_leave", nullable = false)
    @Builder.Default
    private Boolean sickLeave = false;

    /**
     * Number of sick leave days (брой дни болничен)
     */
    @Column(name = "sick_leave_days")
    private Integer sickLeaveDays;

    /**
     * Sick leave start date (от коя дата започва болничния)
     */
    @Column(name = "sick_leave_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sickLeaveStartDate;

    /**
     * Medicines prescribed in this recipe (назначено лечение/лекарства)
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeMedicine> recipeMedicines = new ArrayList<>();

    /**
     * Diagnoses made during this examination (диагнози поставени при този преглед)
     * One or more diagnoses can be made during an examination
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis> diagnoses = new ArrayList<>();

    /**
     * Sick leaves issued during this examination (болнични издадени при този преглед)
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.inf.cscb869_pharmacy.sickleave.entity.SickLeave> sickLeaves = new ArrayList<>();

    /**
     * Calculate sick leave end date (изчислена крайна дата на болничен)
     */
    public LocalDate getSickLeaveEndDate() {
        if (sickLeaveStartDate != null && sickLeaveDays != null && sickLeaveDays > 0) {
            return sickLeaveStartDate.plusDays(sickLeaveDays - 1);
        }
        return null;
    }

    /**
     * Check if currently on sick leave
     */
    public boolean isCurrentlyOnSickLeave() {
        if (!Boolean.TRUE.equals(sickLeave) || sickLeaveStartDate == null || sickLeaveDays == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = getSickLeaveEndDate();
        return !today.isBefore(sickLeaveStartDate) && !today.isAfter(endDate);
    }

    /**
     * Helper method to add a medicine to this recipe
     */
    public void addMedicine(RecipeMedicine recipeMedicine) {
        recipeMedicines.add(recipeMedicine);
        recipeMedicine.setRecipe(this);
    }

    /**
     * Helper method to remove a medicine from this recipe
     */
    public void removeMedicine(RecipeMedicine recipeMedicine) {
        recipeMedicines.remove(recipeMedicine);
        recipeMedicine.setRecipe(null);
    }

    /**
     * Helper method to add a diagnosis to this recipe
     */
    public void addDiagnosis(com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis diagnosis) {
        diagnoses.add(diagnosis);
        diagnosis.setRecipe(this);
    }

    /**
     * Helper method to remove a diagnosis from this recipe
     */
    public void removeDiagnosis(com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis diagnosis) {
        diagnoses.remove(diagnosis);
        diagnosis.setRecipe(null);
    }

    /**
     * Helper method to add a sick leave to this recipe
     */
    public void addSickLeave(com.inf.cscb869_pharmacy.sickleave.entity.SickLeave sickLeave) {
        sickLeaves.add(sickLeave);
        sickLeave.setRecipe(this);
    }

    /**
     * Helper method to remove a sick leave from this recipe
     */
    public void removeSickLeave(com.inf.cscb869_pharmacy.sickleave.entity.SickLeave sickLeave) {
        sickLeaves.remove(sickLeave);
        sickLeave.setRecipe(null);
    }

    /**
     * Check if the recipe is expired
     */
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

