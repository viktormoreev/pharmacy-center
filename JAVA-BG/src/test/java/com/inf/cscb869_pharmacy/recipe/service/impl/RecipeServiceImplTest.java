package com.inf.cscb869_pharmacy.recipe.service.impl;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
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
                .diagnosis("Flu")
                .notes("Rest")
                .build();

        when(recipeRepository.save(input)).thenReturn(input);

        Recipe result = recipeService.createRecipe(input);

        assertThat(result).isSameAs(input);
        verify(recipeRepository).save(input);
    }

    @Test
    void updateRecipeShouldUpdateFieldsAndReplaceMedicines() {
        Recipe existing = Recipe.builder()
                .creationDate(LocalDate.of(2026, 1, 1))
                .doctor(doctor("Dr. Old", "UIN-OLD"))
                .customer(customer("Old Patient", "1234567891"))
                .status(RecipeStatus.ACTIVE)
                .diagnosis("Old diagnosis")
                .notes("Old notes")
                .expirationDate(LocalDate.of(2026, 1, 15))
                .recipeMedicines(new ArrayList<>())
                .build();
        existing.getRecipeMedicines().add(recipeMedicine("Old med", "1x daily", 3));

        Recipe update = Recipe.builder()
                .creationDate(LocalDate.of(2026, 2, 10))
                .doctor(doctor("Dr. New", "UIN-NEW"))
                .customer(customer("New Patient", "1234567892"))
                .status(RecipeStatus.FULFILLED)
                .diagnosis("New diagnosis")
                .notes("New notes")
                .expirationDate(LocalDate.of(2026, 3, 1))
                .recipeMedicines(List.of(
                        recipeMedicine("Paracetamol", "2x daily", 5),
                        recipeMedicine("Vitamin C", "1x daily", 10)
                ))
                .build();

        when(recipeRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(existing)).thenReturn(existing);

        Recipe result = recipeService.updateRecipe(update, 42L);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getCreationDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(existing.getDoctor().getName()).isEqualTo("Dr. New");
        assertThat(existing.getCustomer().getName()).isEqualTo("New Patient");
        assertThat(existing.getStatus()).isEqualTo(RecipeStatus.FULFILLED);
        assertThat(existing.getDiagnosis()).isEqualTo("New diagnosis");
        assertThat(existing.getNotes()).isEqualTo("New notes");
        assertThat(existing.getExpirationDate()).isEqualTo(LocalDate.of(2026, 3, 1));

        assertThat(existing.getRecipeMedicines()).hasSize(2);
        assertThat(existing.getRecipeMedicines())
                .extracting(m -> m.getMedicine().getName())
                .containsExactly("Paracetamol", "Vitamin C");
        assertThat(existing.getRecipeMedicines()).allSatisfy(m -> assertThat(m.getRecipe()).isSameAs(existing));

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

    private static Doctor doctor(String name, String license) {
        return Doctor.builder()
                .name(name)
                .licenseNumber(license)
                .specialty("General")
                .isPrimaryDoctor(true)
                .build();
    }

    private static Customer customer(String name, String egn) {
        return Customer.builder()
                .name(name)
                .egn(egn)
                .age(30)
                .build();
    }

    private static RecipeMedicine recipeMedicine(String medicineName, String dosage, int durationDays) {
        Medicine medicine = new Medicine();
        medicine.setName(medicineName);
        return RecipeMedicine.builder()
                .medicine(medicine)
                .dosage(dosage)
                .durationDays(durationDays)
                .quantity(1)
                .build();
    }
}
