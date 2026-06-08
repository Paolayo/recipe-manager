package com.abnamro.recipes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Recipe response payload")
public record RecipeResponse(
        @Schema(description = "Unique identifier", example = "1")
        Long id,
        @Schema(description = "Optimistic lock version — include in PUT requests to detect concurrent modifications")
        Long version,
        @Schema(description = "Name of the recipe", example = "Spaghetti Carbonara")
        String name,
        @Schema(description = "Whether the recipe is vegetarian", example = "false")
        boolean vegetarian,
        @Schema(description = "Number of servings", example = "4")
        int servings,
        @Schema(description = "List of ingredients")
        List<IngredientResponse> ingredients,
        @Schema(description = "Cooking instructions")
        String instructions,
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}