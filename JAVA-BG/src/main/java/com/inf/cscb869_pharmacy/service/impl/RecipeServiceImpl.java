package com.inf.cscb869_pharmacy.service.impl;

import com.inf.cscb869_pharmacy.data.entity.Recipe;
import com.inf.cscb869_pharmacy.data.repo.RecipeRepository;
import com.inf.cscb869_pharmacy.service.RecipeService;
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
                .map(recipe1 -> {
                    recipe1.setCreationDate(recipe.getCreationDate());
                    return this.recipeRepository.save(recipe1);
                }).orElseGet(() ->
                        this.recipeRepository.save(recipe)
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
}
