package com.inf.cscb869_pharmacy.recipe.service.impl;

import com.inf.cscb869_pharmacy.recipe.dto.RecipeDTO;
import com.inf.cscb869_pharmacy.recipe.dto.RecipeMedicineDTO;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.repository.RecipeRepository;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;

    @Override
    public List<Recipe> getRecipes() {
        return this.recipeRepository.findAll();
    }

    @Override
    public Recipe getRecipe(long id) {
        return this.recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe with id=" + id + " not found!"));
    }

    @Override
    public RecipeDTO createRecipe(Recipe recipe) {
        validateRecipe(recipe);
        Recipe savedRecipe = this.recipeRepository.save(recipe);
        return toDto(savedRecipe);
    }

    @Override
    public RecipeDTO updateRecipe(Recipe recipe, long id) {
        Recipe existingRecipe = this.recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe with id=" + id + " not found!"));

        validateRecipe(recipe);

        existingRecipe.setCreationDate(recipe.getCreationDate());
        existingRecipe.setDoctor(recipe.getDoctor());
        existingRecipe.setCustomer(recipe.getCustomer());
        existingRecipe.setStatus(recipe.getStatus());
        existingRecipe.setDiagnosis(recipe.getDiagnosis());
        existingRecipe.setNotes(recipe.getNotes());
        existingRecipe.setExpirationDate(recipe.getExpirationDate());

        existingRecipe.getRecipeMedicines().clear();
        if (recipe.getRecipeMedicines() != null) {
            recipe.getRecipeMedicines().forEach(existingRecipe::addMedicine);
        }

        Recipe savedRecipe = this.recipeRepository.save(existingRecipe);
        return toDto(savedRecipe);
    }

    @Override
    public void deleteRecipe(long id) {
        this.recipeRepository.deleteById(id);
    }

    @Override
    public List<Recipe> getAllRecipesByCreationDateAndDoctorId(LocalDate creationDate, long id) {
        return this.recipeRepository.findAllByCreationDateAndDoctorId(creationDate, id);
    }

    @Override
    public List<Recipe> getAllRecipesByCreationDateAndDoctorNameContains(LocalDate creationDate, String doctorName) {
        return this.recipeRepository.findAllByCreationDateAndDoctorNameContains(creationDate, doctorName);
    }

    @Override
    public long countRecipes() {
        return this.recipeRepository.count();
    }

    @Override
    public long countRecipesWithDiagnosis() {
        return this.recipeRepository.countWithDiagnosis();
    }

    @Override
    public long countRecipesByStatus(String status) {
        return this.recipeRepository.findAll().stream()
                .filter(recipe -> recipe.getStatus() != null && recipe.getStatus().name().equalsIgnoreCase(status))
                .count();
    }

    private void validateRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe is required");
        }
        if (recipe.getCreationDate() == null) {
            throw new IllegalArgumentException("Creation date is required");
        }
        if (recipe.getCreationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Creation date cannot be in the future");
        }
        if (recipe.getDoctor() == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (recipe.getCustomer() == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (recipe.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (recipe.getExpirationDate() != null && recipe.getExpirationDate().isBefore(recipe.getCreationDate())) {
            throw new IllegalArgumentException("Expiration date cannot be before creation date");
        }
        if (recipe.getRecipeMedicines() != null) {
            for (var recipeMedicine : recipe.getRecipeMedicines()) {
                if (recipeMedicine == null) {
                    throw new IllegalArgumentException("Recipe medicine entry is invalid");
                }
                if (recipeMedicine.getMedicine() == null) {
                    throw new IllegalArgumentException("Medicine is required");
                }
                if (recipeMedicine.getDosage() == null || recipeMedicine.getDosage().isBlank()) {
                    throw new IllegalArgumentException("Dosage is required");
                }
                if (recipeMedicine.getDurationDays() == null || recipeMedicine.getDurationDays() < 1) {
                    throw new IllegalArgumentException("Duration must be at least 1 day");
                }
                if (recipeMedicine.getQuantity() == null || recipeMedicine.getQuantity() < 1) {
                    throw new IllegalArgumentException("Quantity must be at least 1");
                }
            }
        }
    }

    private RecipeDTO toDto(Recipe recipe) {
        RecipeDTO dto = RecipeDTO.builder()
                .id(recipe.getId())
                .creationDate(recipe.getCreationDate())
                .doctorId(recipe.getDoctor() != null ? recipe.getDoctor().getId() : null)
                .customerId(recipe.getCustomer() != null ? recipe.getCustomer().getId() : null)
                .status(recipe.getStatus())
                .diagnosis(recipe.getDiagnosis())
                .notes(recipe.getNotes())
                .expirationDate(recipe.getExpirationDate())
                .build();

        if (recipe.getRecipeMedicines() != null) {
            recipe.getRecipeMedicines().forEach(rm -> {
                RecipeMedicineDTO medicineDTO = RecipeMedicineDTO.builder()
                        .id(rm.getId())
                        .medicineId(rm.getMedicine() != null ? rm.getMedicine().getId() : null)
                        .dosage(rm.getDosage())
                        .durationDays(rm.getDurationDays())
                        .instructions(rm.getInstructions())
                        .quantity(rm.getQuantity())
                        .build();
                dto.addMedicine(medicineDTO);
            });
        }

        return dto;
    }
}
