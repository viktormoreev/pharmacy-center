package com.inf.cscb869_pharmacy.recipe.controller;

import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeApiControllerTest {

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeApiController recipeApiController;

    @Test
    void getRecipesShouldReturnServiceResult() {
        // Arrange
        List<Recipe> recipes = List.of(new Recipe(), new Recipe());
        when(recipeService.getRecipes()).thenReturn(recipes);

        // Act
        List<Recipe> result = recipeApiController.getRecipes();

        // Assert
        assertThat(result).isEqualTo(recipes);
    }

    @Test
    void createRecipeShouldDelegateToService() {
        // Arrange
        Recipe recipe = new Recipe();
        when(recipeService.createRecipe(recipe)).thenReturn(recipe);

        // Act
        Recipe result = recipeApiController.createRecipe(recipe);

        // Assert
        assertThat(result).isSameAs(recipe);
        verify(recipeService).createRecipe(recipe);
    }

    @Test
    void updateRecipeShouldDelegateToService() {
        // Arrange
        Recipe recipe = new Recipe();
        when(recipeService.updateRecipe(recipe, 5L)).thenReturn(recipe);

        // Act
        Recipe result = recipeApiController.updateRecipe(recipe, 5L);

        // Assert
        assertThat(result).isSameAs(recipe);
        verify(recipeService).updateRecipe(recipe, 5L);
    }

    @Test
    void deleteRecipeShouldDelegateToService() {
        // Act
        recipeApiController.deleteRecipe(10L);

        // Assert
        verify(recipeService).deleteRecipe(10L);
    }

    @Test
    void findByDateAndDoctorFiltersShouldDelegateToService() {
        // Arrange: both query variants return same test payload.
        LocalDate date = LocalDate.of(2026, 2, 10);
        List<Recipe> recipes = List.of(new Recipe());
        when(recipeService.getAllRecipesByCreationDateAndDoctorId(date, 2L)).thenReturn(recipes);
        when(recipeService.getAllRecipesByCreationDateAndDoctorNameContains(date, "Smith")).thenReturn(recipes);

        // Act
        List<Recipe> byId = recipeApiController.getAllRecipesByCreationDateAndDoctorId(date, 2L);
        List<Recipe> byName = recipeApiController.getAllRecipesByCreationDateAndDoctorId(date, "Smith");

        // Assert
        assertThat(byId).isEqualTo(recipes);
        assertThat(byName).isEqualTo(recipes);
        verify(recipeService).getAllRecipesByCreationDateAndDoctorId(date, 2L);
        verify(recipeService).getAllRecipesByCreationDateAndDoctorNameContains(date, "Smith");
    }
}
