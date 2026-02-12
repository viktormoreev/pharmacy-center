package com.inf.cscb869_pharmacy.customer.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "EGN is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "EGN must be exactly 10 digits")
    @Column(unique = true, nullable = false, length = 10)
    private String egn;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id", nullable = false)
    @NotNull(message = "Primary doctor is required - every patient must have a personal doctor")
    private Doctor primaryDoctor;

    @Column(name = "insurance_paid_until")
    private LocalDate insurancePaidUntil;

    @Column(length = 1000)
    private String allergies;

    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recipe> recipes = new ArrayList<>();

    public boolean hasValidInsurance() {
        if (insurancePaidUntil == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !insurancePaidUntil.isBefore(today);
    }

    @Transient
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Math.max(0, Period.between(dateOfBirth, LocalDate.now()).getYears());
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", egn='" + egn + '\'' +
                ", age=" + getAge() +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                ", hasValidInsurance=" + hasValidInsurance() +
                '}';
    }
}
