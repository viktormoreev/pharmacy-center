package com.inf.cscb869_pharmacy.doctor.controller;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorViewControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorViewController doctorViewController;

    @Test
    void listDoctorsShouldPopulateModelAndReturnTemplate() {
        // Arrange
        List<Doctor> doctors = List.of(doctor("Dr. A"), doctor("Dr. B"));
        when(doctorService.getDoctors()).thenReturn(doctors);

        // Act
        ExtendedModelMap model = new ExtendedModelMap();
        String view = doctorViewController.listDoctors(model);

        // Assert
        assertThat(view).isEqualTo("doctors/doctors");
        assertThat(model.getAttribute("doctors")).isEqualTo(doctors);
    }

    @Test
    void createDoctorShouldReturnFormWhenValidationHasErrors() {
        // Arrange: invalid binding state.
        Doctor doctor = doctor("Dr. Invalid");
        BindingResult bindingResult = new BeanPropertyBindingResult(doctor, "doctor");
        bindingResult.rejectValue("name", "invalid", "Invalid name");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = doctorViewController.createDoctor(doctor, bindingResult, redirect);

        // Assert
        assertThat(view).isEqualTo("doctors/create-doctor");
        verifyNoInteractions(doctorService);
    }

    @Test
    void createDoctorShouldPersistAndRedirectOnSuccess() {
        // Arrange
        Doctor doctor = doctor("Dr. New");
        BindingResult bindingResult = new BeanPropertyBindingResult(doctor, "doctor");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = doctorViewController.createDoctor(doctor, bindingResult, redirect);

        // Assert
        assertThat(view).isEqualTo("redirect:/doctors");
        assertThat(redirect.getFlashAttributes().get("successMessage")).isEqualTo("Doctor created successfully!");
        verify(doctorService).createDoctor(doctor);
    }

    @Test
    void viewDoctorShouldRedirectWhenDoctorMissing() {
        // Arrange: service throws when doctor does not exist.
        when(doctorService.getDoctor(99L)).thenThrow(new RuntimeException("missing"));
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = doctorViewController.viewDoctor(99L, model, redirect);

        // Assert: user is redirected with user-friendly error.
        assertThat(view).isEqualTo("redirect:/doctors");
        assertThat(redirect.getFlashAttributes().get("errorMessage")).isEqualTo("Doctor not found!");
    }

    private static Doctor doctor(String name) {
        return Doctor.builder()
                .name(name)
                .licenseNumber("UIN-" + name.replace(" ", ""))
                .specialty("General")
                .isPrimaryDoctor(true)
                .build();
    }
}
