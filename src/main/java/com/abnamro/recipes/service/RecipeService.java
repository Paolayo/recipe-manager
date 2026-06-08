package com.abnamro.recipes.service;

import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;

import java.util.List;

/**
 * Service contract for managing recipes.
 */
public interface RecipeService {

    /**
     * Persists a new recipe and returns the created resource.
     */
    RecipeResponse createRecipe(RecipeRequest request);

    /**
     * Returns the recipe with the given ID.
     *
     * @throws com.abnamro.recipes.exception.RecipeNotFoundException if no recipe exists with that ID
     */
    RecipeResponse getRecipeById(Long id);

    /**
     * Returns all recipes in the system.
     */
    List<RecipeResponse> getAllRecipes();

    /**
     * Replaces the recipe identified by {@code id} with the provided data.
     *
     * @throws com.abnamro.recipes.exception.RecipeNotFoundException if no recipe exists with that ID
     */
    RecipeResponse updateRecipe(Long id, RecipeRequest request);

    /**
     * Removes the recipe with the given ID.
     *
     * @throws com.abnamro.recipes.exception.RecipeNotFoundException if no recipe exists with that ID
     */
    void deleteRecipe(Long id);

    /**
     * Returns recipes matching every criterion set in {@code filter}; unset fields are ignored.
     */
    List<RecipeResponse> filterRecipes(RecipeFilter filter);
}
