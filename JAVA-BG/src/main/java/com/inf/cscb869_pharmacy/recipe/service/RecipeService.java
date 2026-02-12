package com.inf.cscb869_pharmacy.recipe.service;

import com.inf.cscb869_pharmacy.recipe.dto.RecipeDTO;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;

import java.time.LocalDate;
import java.util.List;

public interface RecipeService {
    List<Recipe> getRecipes();

    Recipe getRecipe(long id);

    RecipeDTO createRecipe(Recipe recipe);

    RecipeDTO updateRecipe(Recipe recipe, long id);

    void deleteRecipe(long id);

    List<Recipe> getAllRecipesByCreationDateAndDoctorId(LocalDate creationDate, long id);

    List<Recipe> getAllRecipesByCreationDateAndDoctorNameContains(LocalDate creationDate, String doctorName);
    
    long countRecipes();
    long countRecipesWithDiagnosis();
    long countRecipesByStatus(String status);
}
