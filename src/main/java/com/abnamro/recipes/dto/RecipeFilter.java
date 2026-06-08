package com.abnamro.recipes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Filter criteria for searching recipes")
public record RecipeFilter(
        @Schema(description = "Filter by vegetarian status", example = "true")
        Boolean vegetarian,
        @Schema(description = "Filter by exact number of servings", example = "4")
        Integer servings,
        @Schema(description = "Ingredients that must be included", example = "[\"potatoes\"]")
        List<String> includeIngredients,
        @Schema(description = "Ingredients that must be excluded", example = "[\"salmon\"]")
        List<String> excludeIngredients,
        @Schema(description = "Text to search within instructions", example = "oven")
        String instructionSearch
) {}