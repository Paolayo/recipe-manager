package com.abnamro.recipes.exception;

/**
 * Thrown when attempting to create or rename a recipe to a name that already exists.
 */
public class DuplicateRecipeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecipeException(String name) {
        super("A recipe with the name '" + name + "' already exists");
    }
}