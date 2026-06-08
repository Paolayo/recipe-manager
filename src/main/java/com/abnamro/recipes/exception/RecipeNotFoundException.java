package com.abnamro.recipes.exception;

/**
 * Thrown when a recipe with a given ID does not exist in the database.
 */
public class RecipeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecipeNotFoundException(Long id) {
        super("Recipe not found with id: " + id);
    }
}
