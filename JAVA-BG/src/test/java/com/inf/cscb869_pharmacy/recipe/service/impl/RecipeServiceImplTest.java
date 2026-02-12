package com.inf.cscb869_pharmacy.recipe.service.impl;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.recipe.dto.RecipeDTO;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.entity.RecipeMedicine;
import com.inf.cscb869_pharmacy.recipe.entity.RecipeStatus;
import com.inf.cscb869_pharmacy.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    @Test
    void createRecipeShouldSaveAndReturnRecipe() {
        Recipe input = Recipe.builder()
                .creationDate(LocalDate.of(2026, 2, 10))
                .doctor(doctor("Dr. A", "UIN-1"))
                .customer(customer("Alice", "1234567890"))
                .status(RecipeStatus.ACTIVE)
                .diagnoses(new ArrayList<>(List.of(diagnosis("Flu"))))
                .notes("Rest")
                .build();
        input.setId(10L);
        input.getDiagnoses().forEach(d -> d.setRecipe(input));

        when(recipeRepository.save(input)).thenReturn(input);
        RecipeDTO result = recipeService.createRecipe(input);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDoctorId()).isNotNull();
        assertThat(result.getCustomerId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RecipeStatus.ACTIVE);
        verify(recipeRepository).save(input);
    }

    @Test
    void updateRecipeShouldUpdateFieldsAndReplaceMedicines() {
        Recipe existing = Recipe.builder()
                .creationDate(LocalDate.of(2026, 1, 1))
                .doctor(doctor("Dr. Old", "UIN-OLD"))
                .customer(customer("Old Patient", "1234567891"))
                .status(RecipeStatus.ACTIVE)
                .diagnoses(new ArrayList<>(List.of(diagnosis("Old diagnosis"))))
                .notes("Old notes")
                .expirationDate(LocalDate.of(2026, 1, 15))
                .recipeMedicines(new ArrayList<>())
                .build();
        existing.setId(42L);
        existing.getDiagnoses().forEach(d -> d.setRecipe(existing));
        existing.getRecipeMedicines().add(recipeMedicine("Old med", "1x daily", 3));

        Recipe update = Recipe.builder()
                .creationDate(LocalDate.of(2026, 2, 10))
                .doctor(doctor("Dr. New", "UIN-NEW"))
                .customer(customer("New Patient", "1234567892"))
                .status(RecipeStatus.FULFILLED)
                .diagnoses(new ArrayList<>(List.of(diagnosis("New diagnosis"))))
                .notes("New notes")
                .expirationDate(LocalDate.of(2026, 3, 1))
                .recipeMedicines(List.of(
                        recipeMedicine("Paracetamol", "2x daily", 5),
                        recipeMedicine("Vitamin C", "1x daily", 10)
                ))
                .build();
        update.getDiagnoses().forEach(d -> d.setRecipe(update));

        when(recipeRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(existing)).thenReturn(existing);
        RecipeDTO result = recipeService.updateRecipe(update, 42L);
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(existing.getCreationDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(existing.getDoctor().getName()).isEqualTo("Dr. New");
        assertThat(existing.getCustomer().getName()).isEqualTo("New Patient");
        assertThat(existing.getStatus()).isEqualTo(RecipeStatus.FULFILLED);
        assertThat(existing.getNotes()).isEqualTo("New notes");
        assertThat(existing.getExpirationDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(existing.getDiagnoses()).extracting(Diagnosis::getName).containsExactly("New diagnosis");

        assertThat(existing.getRecipeMedicines()).hasSize(2);
        assertThat(existing.getRecipeMedicines())
                .extracting(m -> m.getMedicine().getName())
                .containsExactly("Paracetamol", "Vitamin C");
        assertThat(existing.getRecipeMedicines()).allSatisfy(m -> assertThat(m.getRecipe()).isSameAs(existing));
        assertThat(result.getMedicines()).hasSize(2);

        ArgumentCaptor<Recipe> saveCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).isSameAs(existing);
    }

    @Test
    void updateRecipeShouldThrowWhenRecipeNotFound() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> recipeService.updateRecipe(Recipe.builder().build(), 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Recipe with id=999 not found!");
    }

    @Test
    void createRecipeShouldRejectFutureCreationDate() {
        Recipe input = Recipe.builder()
                .creationDate(LocalDate.now().plusDays(1))
                .doctor(doctor("Dr. A", "UIN-1"))
                .customer(customer("Alice", "1234567890"))
                .status(RecipeStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> recipeService.createRecipe(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Creation date cannot be in the future");
    }

    @Test
    void updateRecipeShouldRejectInvalidMedicineRow() {
        Recipe existing = Recipe.builder()
                .creationDate(LocalDate.of(2026, 1, 1))
                .doctor(doctor("Dr. Old", "UIN-OLD"))
                .customer(customer("Old Patient", "1234567891"))
                .status(RecipeStatus.ACTIVE)
                .recipeMedicines(new ArrayList<>())
                .build();

        Recipe update = Recipe.builder()
                .creationDate(LocalDate.of(2026, 2, 10))
                .doctor(doctor("Dr. New", "UIN-NEW"))
                .customer(customer("New Patient", "1234567892"))
                .status(RecipeStatus.ACTIVE)
                .recipeMedicines(List.of(
                        RecipeMedicine.builder()
                                .medicine(new Medicine())
                                .dosage("1x daily")
                                .durationDays(5)
                                .quantity(0)
                                .build()
                ))
                .build();

        when(recipeRepository.findById(42L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> recipeService.updateRecipe(update, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be at least 1");
    }

    private static Doctor doctor(String name, String license) {
        Doctor doctor = Doctor.builder()
                .name(name)
                .licenseNumber(license)
                .specialty("General")
                .isPrimaryDoctor(true)
                .build();
        doctor.setId((long) Math.abs(license.hashCode()));
        return doctor;
    }

    private static Customer customer(String name, String egn) {
        Customer customer = Customer.builder()
                .name(name)
                .egn(egn)
                .build();
        customer.setId((long) Math.abs(egn.hashCode()));
        return customer;
    }

    private static RecipeMedicine recipeMedicine(String medicineName, String dosage, int durationDays) {
        Medicine medicine = new Medicine();
        medicine.setId((long) medicineName.hashCode());
        medicine.setName(medicineName);
        return RecipeMedicine.builder()
                .medicine(medicine)
                .dosage(dosage)
                .durationDays(durationDays)
                .quantity(1)
                .build();
    }

    private static Diagnosis diagnosis(String name) {
        return Diagnosis.builder()
                .name(name)
                .diagnosisDate(LocalDate.of(2026, 2, 10))
                .isPrimary(true)
                .build();
    }
}
