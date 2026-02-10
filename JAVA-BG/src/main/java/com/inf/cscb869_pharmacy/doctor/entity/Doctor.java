package com.inf.cscb869_pharmacy.doctor.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Doctor entity representing a medical professional in the system.
 * Doctors can examine patients, issue prescriptions, and serve as primary doctors.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @NotBlank(message = "Doctor name is required")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique identification number (УИН) for the doctor
     */
    @NotBlank(message = "License number is required")
    @Column(name = "license_number", unique = true, nullable = false, length = 20)
    private String licenseNumber;

    /**
     * Medical specialty (e.g., Cardiologist, Pediatrician, General Practitioner)
     */
    @NotBlank(message = "Specialty is required")
    @Column(nullable = false, length = 100)
    private String specialty;

    /**
     * Indicates if this doctor can be registered as a primary doctor (личен лекар)
     */
    @Column(name = "is_primary_doctor", nullable = false)
    @Builder.Default
    private Boolean isPrimaryDoctor = true;

    /**
     * Doctor's email address
     */
    @Email(message = "Email should be valid")
    @Column(length = 100)
    private String email;

    /**
     * Doctor's phone number
     */
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number should be valid")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * All examinations/prescriptions issued by this doctor
     */
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Recipe> recipes = new HashSet<>();

    /**
     * Patients who have this doctor as their primary doctor (личен лекар)
     */
    @OneToMany(mappedBy = "primaryDoctor", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Customer> primaryPatients = new HashSet<>();
}
