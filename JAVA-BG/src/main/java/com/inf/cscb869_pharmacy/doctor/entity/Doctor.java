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

    @NotBlank(message = "License number is required")
    @Column(name = "license_number", unique = true, nullable = false, length = 20)
    private String licenseNumber;

    @NotBlank(message = "Specialty is required")
    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(name = "is_primary_doctor", nullable = false)
    @Builder.Default
    private Boolean isPrimaryDoctor = true;

    @Email(message = "Email should be valid")
    @Column(length = 100)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number should be valid")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Recipe> recipes = new HashSet<>();

    @OneToMany(mappedBy = "primaryDoctor", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Customer> primaryPatients = new HashSet<>();
}
