package com.inf.cscb869_pharmacy.sickleave.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import com.inf.cscb869_pharmacy.sickleave.repository.SickLeaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SickLeaveServiceImplTest {

    @Mock
    private SickLeaveRepository sickLeaveRepository;

    @InjectMocks
    private SickLeaveServiceImpl sickLeaveService;

    @Test
    void createSickLeaveShouldApplyDefaultsAndSave() {
        SickLeave sickLeave = SickLeave.builder()
                .customer(customer(1L))
                .doctor(doctor())
                .recipe(Recipe.builder().creationDate(LocalDate.now()).build())
                .startDate(LocalDate.of(2026, 2, 1))
                .durationDays(5)
                .reason("Flu")
                .build();

        when(sickLeaveRepository.save(sickLeave)).thenReturn(sickLeave);

        SickLeave result = sickLeaveService.createSickLeave(sickLeave);

        assertThat(result).isSameAs(sickLeave);
        assertThat(sickLeave.getStatus()).isEqualTo(SickLeaveStatus.ACTIVE);
        assertThat(sickLeave.getIssueDate()).isEqualTo(LocalDate.now());
        assertThat(sickLeave.getLeaveNumber()).isNotBlank();
        assertThat(Pattern.matches("^SL-\\d{8}-[A-Z0-9]{4}$", sickLeave.getLeaveNumber())).isTrue();
        verify(sickLeaveRepository).save(sickLeave);
    }

    @Test
    void updateSickLeaveShouldUpdateMutableFields() {
        SickLeave existing = SickLeave.builder()
                .startDate(LocalDate.of(2026, 2, 1))
                .durationDays(3)
                .reason("Old")
                .status(SickLeaveStatus.ACTIVE)
                .notes("Old notes")
                .build();

        SickLeave update = SickLeave.builder()
                .startDate(LocalDate.of(2026, 2, 10))
                .durationDays(7)
                .reason("New reason")
                .status(SickLeaveStatus.EXTENDED)
                .notes("New notes")
                .build();

        when(sickLeaveRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(sickLeaveRepository.save(existing)).thenReturn(existing);

        SickLeave result = sickLeaveService.updateSickLeave(10L, update);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(existing.getDurationDays()).isEqualTo(7);
        assertThat(existing.getReason()).isEqualTo("New reason");
        assertThat(existing.getStatus()).isEqualTo(SickLeaveStatus.EXTENDED);
        assertThat(existing.getNotes()).isEqualTo("New notes");
        verify(sickLeaveRepository).save(existing);
    }

    @Test
    void extendSickLeaveShouldIncreaseDaysAndAppendNote() {
        SickLeave existing = SickLeave.builder()
                .durationDays(5)
                .status(SickLeaveStatus.ACTIVE)
                .notes("Initial")
                .build();

        when(sickLeaveRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(sickLeaveRepository.save(existing)).thenReturn(existing);

        SickLeave result = sickLeaveService.extendSickLeave(2L, 3, "Still ill");

        assertThat(result.getDurationDays()).isEqualTo(8);
        assertThat(result.getStatus()).isEqualTo(SickLeaveStatus.EXTENDED);
        assertThat(result.getNotes()).contains("Initial");
        assertThat(result.getNotes()).contains("Extended by 3 days");
        assertThat(result.getNotes()).contains("Reason: Still ill");
    }

    @Test
    void cancelAndCompleteShouldSetExpectedStatuses() {
        SickLeave cancellable = SickLeave.builder().status(SickLeaveStatus.ACTIVE).build();
        SickLeave completable = SickLeave.builder().status(SickLeaveStatus.EXTENDED).build();

        when(sickLeaveRepository.findById(3L)).thenReturn(Optional.of(cancellable));
        when(sickLeaveRepository.findById(4L)).thenReturn(Optional.of(completable));
        when(sickLeaveRepository.save(cancellable)).thenReturn(cancellable);
        when(sickLeaveRepository.save(completable)).thenReturn(completable);

        SickLeave cancelled = sickLeaveService.cancelSickLeave(3L, "Wrong document");
        SickLeave completed = sickLeaveService.completeSickLeave(4L);

        assertThat(cancelled.getStatus()).isEqualTo(SickLeaveStatus.CANCELLED);
        assertThat(cancelled.getNotes()).contains("Reason: Wrong document");
        assertThat(completed.getStatus()).isEqualTo(SickLeaveStatus.COMPLETED);
        verify(sickLeaveRepository, times(2)).save(org.mockito.ArgumentMatchers.any(SickLeave.class));
    }

    @Test
    void getSickLeaveByIdShouldThrowWhenMissing() {
        when(sickLeaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sickLeaveService.getSickLeaveById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sick leave not found with ID: 999");
    }

    private static Customer customer(Long id) {
        Customer customer = Customer.builder()
                .name("Patient")
                .egn("1234567890")
                .age(30)
                .build();
        customer.setId(id);
        return customer;
    }

    private static Doctor doctor() {
        return Doctor.builder()
                .name("Doctor")
                .licenseNumber("UIN-1")
                .specialty("General")
                .isPrimaryDoctor(true)
                .build();
    }
}
