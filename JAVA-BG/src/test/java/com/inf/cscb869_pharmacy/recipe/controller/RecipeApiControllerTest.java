package com.inf.cscb869_pharmacy.recipe.controller;

import com.inf.cscb869_pharmacy.recipe.dto.RecipeDTO;
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
        List<Recipe> recipes = List.of(new Recipe(), new Recipe());
        when(recipeService.getRecipes()).thenReturn(recipes);
        List<Recipe> result = recipeApiController.getRecipes();
        assertThat(result).isEqualTo(recipes);
    }

    @Test
    void createRecipeShouldDelegateToService() {
        Recipe recipe = new Recipe();
        RecipeDTO dto = RecipeDTO.builder().id(1L).build();
        when(recipeService.createRecipe(recipe)).thenReturn(dto);
        RecipeDTO result = recipeApiController.createRecipe(recipe);
        assertThat(result).isSameAs(dto);
        verify(recipeService).createRecipe(recipe);
    }

    @Test
    void updateRecipeShouldDelegateToService() {
        Recipe recipe = new Recipe();
        RecipeDTO dto = RecipeDTO.builder().id(5L).build();
        when(recipeService.updateRecipe(recipe, 5L)).thenReturn(dto);
        RecipeDTO result = recipeApiController.updateRecipe(recipe, 5L);
        assertThat(result).isSameAs(dto);
        verify(recipeService).updateRecipe(recipe, 5L);
    }

    @Test
    void deleteRecipeShouldDelegateToService() {
        recipeApiController.deleteRecipe(10L);
        verify(recipeService).deleteRecipe(10L);
    }

    @Test
    void findByDateAndDoctorFiltersShouldDelegateToService() {
        LocalDate date = LocalDate.of(2026, 2, 10);
        List<Recipe> recipes = List.of(new Recipe());
        when(recipeService.getAllRecipesByCreationDateAndDoctorId(date, 2L)).thenReturn(recipes);
        when(recipeService.getAllRecipesByCreationDateAndDoctorNameContains(date, "Smith")).thenReturn(recipes);
        List<Recipe> byId = recipeApiController.getAllRecipesByCreationDateAndDoctorId(date, 2L);
        List<Recipe> byName = recipeApiController.getAllRecipesByCreationDateAndDoctorId(date, "Smith");
        assertThat(byId).isEqualTo(recipes);
        assertThat(byName).isEqualTo(recipes);
        verify(recipeService).getAllRecipesByCreationDateAndDoctorId(date, 2L);
        verify(recipeService).getAllRecipesByCreationDateAndDoctorNameContains(date, "Smith");
    }
}
