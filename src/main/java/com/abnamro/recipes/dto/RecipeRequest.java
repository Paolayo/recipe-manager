package com.abnamro.recipes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request payload for creating or updating a recipe")
public record RecipeRequest(
        @NotBlank(message = "Recipe name is required")
        @Schema(description = "Name of the recipe", example = "Spaghetti Carbonara")
        String name,

        @NotNull(message = "Vegetarian flag is required")
        @Schema(description = "Whether the recipe is vegetarian", example = "false")
        Boolean vegetarian,

        @Min(value = 1, message = "Servings must be at least 1")
        @Schema(description = "Number of servings", example = "4")
        int servings,

        @NotEmpty(message = "At least one ingredient is required")
        @Schema(description = "List of ingredients", example = "[\"pasta\", \"eggs\", \"pancetta\", \"parmesan\"]")
        List<String> ingredients,

        @NotBlank(message = "Instructions are required")
        @Schema(description = "Cooking instructions", example = "Boil pasta. Mix eggs with cheese. Combine with pancetta.")
        String instructions
) {}