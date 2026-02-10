package com.inf.cscb869_pharmacy.customer.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer/Patient entity representing a patient in the medical system.
 * Contains personal information, medical history, allergies, and insurance data.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * EGN - Bulgarian National ID (Единен Граждански Номер)
     * Must be exactly 10 digits
     */
    @NotBlank(message = "EGN is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "EGN must be exactly 10 digits")
    @Column(unique = true, nullable = false, length = 10)
    private String egn;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be realistic")
    @Column(nullable = false)
    private Integer age;

    @Email(message = "Email should be valid")
    @Column(unique = true, length = 100)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number should be valid")
    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Primary doctor (личен лекар) - Every patient must be registered with a primary doctor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id", nullable = false)
    @NotNull(message = "Primary doctor is required - every patient must have a personal doctor")
    private Doctor primaryDoctor;

    /**
     * Insurance paid until date - used to check if patient has valid insurance (last 6 months)
     * Здравна осигуровка платена до (дата)
     */
    @Column(name = "insurance_paid_until")
    private LocalDate insurancePaidUntil;

    /**
     * Medical allergies (comma-separated or formatted text)
     * Example: "Penicillin, Aspirin, Peanuts"
     */
    @Column(length = 1000)
    private String allergies;

    /**
     * Medical conditions or history
     * Example: "Diabetes Type 2, Hypertension"
     */
    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    /**
     * National ID or Insurance Number
     */
    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;

    /**
     * Is the customer currently active?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Recipes/Prescriptions/Examinations associated with this patient (medical history)
     * История на заболяванията
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recipe> recipes = new ArrayList<>();

    /**
     * Check if patient has valid health insurance (paid within last 6 months)
     * Проверка дали е платена здравна осигуровка през последните 6 месеца
     */
    public boolean hasValidInsurance() {
        if (insurancePaidUntil == null) {
            return false;
        }
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return !insurancePaidUntil.isBefore(sixMonthsAgo);
    }

    /**
     * Helper method to add a recipe/examination to this customer's history
     */
    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        recipe.setCustomer(this);
    }

    /**
     * Helper method to remove a recipe from this customer
     */
    public void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
        recipe.setCustomer(null);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", egn='" + egn + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                ", hasValidInsurance=" + hasValidInsurance() +
                '}';
    }
}
