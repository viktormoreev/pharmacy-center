package com.inf.cscb869_pharmacy.recipe.service;

import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;

import java.time.LocalDate;
import java.util.List;

public interface RecipeService {
    List<Recipe> getRecipes();

    Recipe getRecipe(long id);

    Recipe createRecipe(Recipe recipe);

    Recipe updateRecipe(Recipe recipe, long id);

    void deleteRecipe(long id);

    List<Recipe> getAllRecipesByCreationDateAndDoctorId(LocalDate creationDate, long id);

    List<Recipe> getAllRecipesByCreationDateAndDoctorNameContains(LocalDate creationDate, String doctorName);
    
    long countRecipes();
    long countRecipesByStatus(String status);
}