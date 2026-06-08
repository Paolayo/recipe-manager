package com.abnamro.recipes.controller;

import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing the recipe management API under {@code /api/v1/recipes}.
 * Handles HTTP concerns only (request mapping, validation, status codes) and delegates
 * all business logic to {@link com.abnamro.recipes.service.RecipeService}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "API for managing favourite recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @Operation(summary = "Create a new recipe")
    @ApiResponse(responseCode = "201", description = "Recipe created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(
            @Valid @RequestBody RecipeRequest request) {
        log.debug("POST /api/v1/recipes - creating recipe: {}", request.name());
        RecipeResponse response = recipeService.createRecipe(request);
        log.debug("POST /api/v1/recipes - created recipe with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a recipe by ID")
    @ApiResponse(responseCode = "200", description = "Recipe found")
    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(
            @Parameter(description = "Recipe ID") @PathVariable Long id) {
        log.debug("GET /api/v1/recipes/{}", id);
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @Operation(summary = "Get all recipes")
    @ApiResponse(responseCode = "200", description = "List of all recipes")
    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        log.debug("GET /api/v1/recipes");
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @Operation(summary = "Update an existing recipe")
    @ApiResponse(responseCode = "200", description = "Recipe updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        log.debug("PUT /api/v1/recipes/{} - updating recipe: {}", id, request.name());
        return ResponseEntity.ok(recipeService.updateRecipe(id, request));
    }

    @Operation(summary = "Delete a recipe")
    @ApiResponse(responseCode = "204", description = "Recipe deleted successfully")
    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @Parameter(description = "Recipe ID") @PathVariable Long id) {
        log.debug("DELETE /api/v1/recipes/{}", id);
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Search and filter recipes",
            description = "Filter recipes by vegetarian status, servings, included/excluded ingredients, and instruction text"
    )
    @ApiResponse(responseCode = "200", description = "Filtered list of recipes")
    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @Parameter(description = "Filter by vegetarian")
            @RequestParam(required = false) Boolean vegetarian,

            @Parameter(description = "Filter by number of servings")
            @RequestParam(required = false) Integer servings,

            @Parameter(description = "Ingredients that must be present (comma-separated)")
            @RequestParam(required = false) List<String> includeIngredients,

            @Parameter(description = "Ingredients that must be absent (comma-separated)")
            @RequestParam(required = false) List<String> excludeIngredients,

            @Parameter(description = "Text to search within instructions")
            @RequestParam(required = false) String instructionSearch) {

        log.debug("GET /api/v1/recipes/search - vegetarian={}, servings={}, include={}, exclude={}, instructions={}",
                vegetarian, servings, includeIngredients, excludeIngredients, instructionSearch);
        RecipeFilter filter = new RecipeFilter(vegetarian, servings, includeIngredients, excludeIngredients, instructionSearch);
        return ResponseEntity.ok(recipeService.filterRecipes(filter));
    }
}
