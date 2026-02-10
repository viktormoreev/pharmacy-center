package com.inf.cscb869_pharmacy.recipe.service.impl;

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
    public Recipe createRecipe(Recipe recipe) {
        return this.recipeRepository.save(recipe);
    }

    @Override
    public Recipe updateRecipe(Recipe recipe, long id) {
        return this.recipeRepository.findById(id)
                .map(existingRecipe -> {
                    // Update all fields
                    existingRecipe.setCreationDate(recipe.getCreationDate());
                    existingRecipe.setDoctor(recipe.getDoctor());
                    existingRecipe.setCustomer(recipe.getCustomer());
                    existingRecipe.setStatus(recipe.getStatus());
                    existingRecipe.setDiagnosis(recipe.getDiagnosis());
                    existingRecipe.setNotes(recipe.getNotes());
                    existingRecipe.setExpirationDate(recipe.getExpirationDate());
                    
                    // Clear existing medicines and add new ones
                    existingRecipe.getRecipeMedicines().clear();
                    if (recipe.getRecipeMedicines() != null) {
                        recipe.getRecipeMedicines().forEach(existingRecipe::addMedicine);
                    }
                    
                    return this.recipeRepository.save(existingRecipe);
                }).orElseThrow(() -> 
                        new RuntimeException("Recipe with id=" + id + " not found!")
                );
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
    public long countRecipesByStatus(String status) {
        return this.recipeRepository.findAll().stream()
                .filter(recipe -> recipe.getStatus() != null && recipe.getStatus().name().equalsIgnoreCase(status))
                .count();
    }
}
