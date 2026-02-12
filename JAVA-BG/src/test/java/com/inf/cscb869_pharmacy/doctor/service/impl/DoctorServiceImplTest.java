package com.inf.cscb869_pharmacy.doctor.service.impl;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void updateDoctorShouldUpdateExistingName() {
        // Arrange: existing record and update payload.
        Doctor existing = doctor("Dr. Old", "old@clinic.com");
        Doctor update = doctor("Dr. New", "new@clinic.com");

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(existing)).thenReturn(existing);

        // Act
        Doctor result = doctorService.updateDoctor(update, 5L);

        // Assert: update applies current implementation behavior.
        assertThat(result).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("Dr. New");
        // Current implementation updates only name.
        assertThat(existing.getEmail()).isEqualTo("old@clinic.com");
        verify(doctorRepository).save(existing);
    }

    @Test
    void updateDoctorShouldSaveInputWhenExistingMissing() {
        // Arrange: no existing row found by id.
        Doctor input = doctor("Dr. Input", "input@clinic.com");
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        when(doctorRepository.save(input)).thenReturn(input);

        // Act
        Doctor result = doctorService.updateDoctor(input, 99L);

        // Assert: fallback path saves incoming object.
        assertThat(result).isSameAs(input);
        verify(doctorRepository).save(input);
    }

    @Test
    void getDoctorShouldThrowWhenMissing() {
        // Arrange
        when(doctorRepository.findById(77L)).thenReturn(Optional.empty());

        // Assert
        assertThatThrownBy(() -> doctorService.getDoctor(77L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Doctor with id=77 not found!");
    }

    @Test
    void findByEmailShouldReturnEmptyForBlankInput() {
        // Blank input is guarded before repository call.
        assertThat(doctorService.findByEmail("  ")).isEmpty();
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void findByEmailShouldDelegateToRepository() {
        // Arrange
        Doctor doctor = doctor("Dr. Delegated", "doc@clinic.com");
        when(doctorRepository.findByEmailIgnoreCase("doc@clinic.com")).thenReturn(Optional.of(doctor));

        // Act
        Optional<Doctor> result = doctorService.findByEmail("doc@clinic.com");

        // Assert
        assertThat(result).contains(doctor);
    }

    private static Doctor doctor(String name, String email) {
        return Doctor.builder()
                .name(name)
                .licenseNumber("UIN-" + name.replace(" ", ""))
                .specialty("General")
                .isPrimaryDoctor(true)
                .email(email)
                .build();
    }
}
