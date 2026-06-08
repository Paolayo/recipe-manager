package com.abnamro.recipes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record IngredientResponse(
        @Schema(description = "Name of the ingredient", example = "Onion")
        String name,
        @Schema(description = "The amount gr", example = "100")
        BigDecimal amount
) {}
