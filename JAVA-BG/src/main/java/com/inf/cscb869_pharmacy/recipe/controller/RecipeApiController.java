package com.inf.cscb869_pharmacy.recipe.controller;

import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeApiController {
    private final RecipeService recipeService;

    @GetMapping
    public List<Recipe> getRecipes() {
        return this.recipeService.getRecipes();
    }

    @GetMapping("/{id}")
    public Recipe getRecipe(@PathVariable long id) {
        return this.recipeService.getRecipe(id);
    }

    @PostMapping
    public Recipe createRecipe(@RequestBody Recipe recipe) {
        return this.recipeService.createRecipe(recipe);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipe(@RequestBody Recipe recipe, @PathVariable long id) {
        return this.recipeService.updateRecipe(recipe, id);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable long id) {
        this.recipeService.deleteRecipe(id);
    }

    @GetMapping("/creation-date/{creationDate}/id/{id}")
    public List<Recipe> getAllRecipesByCreationDateAndDoctorId(@PathVariable LocalDate creationDate,
                                                               @PathVariable long id) {
        return this.recipeService.getAllRecipesByCreationDateAndDoctorId(creationDate, id);
    }

    @GetMapping("/creation-date/{creationDate}/doctor-name-contains/{doctorName}")
    public List<Recipe> getAllRecipesByCreationDateAndDoctorId(@PathVariable LocalDate creationDate,
                                                               @PathVariable String doctorName) {
        return this.recipeService.getAllRecipesByCreationDateAndDoctorNameContains(creationDate, doctorName);
    }
}


