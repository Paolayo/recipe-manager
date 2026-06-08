package com.abnamro.recipes.service;

import com.abnamro.recipes.dto.IngredientResponse;
import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.exception.DuplicateRecipeException;
import com.abnamro.recipes.exception.RecipeNotFoundException;
import com.abnamro.recipes.mapper.RecipeMapper;
import com.abnamro.recipes.model.Ingredient;
import com.abnamro.recipes.model.Recipe;
import com.abnamro.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private Recipe recipe;
    private RecipeRequest request;
    private RecipeResponse response;

    @BeforeEach
    void setUp() {
        recipe = Recipe.builder()
                .id(1L)
                .name("Pasta")
                .vegetarian(true)
                .servings(2)
                .ingredients(new ArrayList<>(List.of(
                        Ingredient.builder().name("pasta").build(),
                        Ingredient.builder().name("tomato").build())))
                .instructions("Boil pasta. Add sauce.")
                .build();

        request = new RecipeRequest("Pasta", true, 2,
                List.of(new IngredientResponse("pasta", null), new IngredientResponse("tomato", null)),
                "Boil pasta. Add sauce.");

        response = new RecipeResponse(1L, null, "Pasta", true, 2, null, null, null, null);
    }

    @Test
    @DisplayName("Should create recipe successfully")
    void createRecipe_success() {
        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = recipeService.createRecipe(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Pasta");
        verify(recipeRepository).save(recipe);
    }

    @Test
    @DisplayName("Should throw DuplicateRecipeException when creating a recipe with an existing name")
    void createRecipe_duplicateName_throwsDuplicateRecipeException() {
        when(recipeRepository.existsByNameIgnoreCase("Pasta")).thenReturn(true);

        assertThatThrownBy(() -> recipeService.createRecipe(request))
                .isInstanceOf(DuplicateRecipeException.class)
                .hasMessageContaining("Pasta");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Should return recipe by ID")
    void getRecipeById_success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = recipeService.getRecipeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw RecipeNotFoundException when recipe not found by ID")
    void getRecipeById_notFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById(99L))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should return all recipes")
    void getAllRecipes_success() {
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeMapper.toResponseList(List.of(recipe))).thenReturn(List.of(response));

        List<RecipeResponse> result = recipeService.getAllRecipes();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should update recipe successfully")
    void updateRecipe_success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = recipeService.updateRecipe(1L, request);

        assertThat(result).isNotNull();
        verify(recipeRepository).flush();
        verify(recipeMapper).updateEntityFromRequest(request, recipe);
        verify(recipeRepository).save(recipe);
    }

    @Test
    @DisplayName("Should throw DuplicateRecipeException when updating a recipe to an existing name")
    void updateRecipe_duplicateName_throwsDuplicateRecipeException() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.existsByNameIgnoreCaseAndIdNot("Pasta", 1L)).thenReturn(true);

        assertThatThrownBy(() -> recipeService.updateRecipe(1L, request))
                .isInstanceOf(DuplicateRecipeException.class)
                .hasMessageContaining("Pasta");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Should throw RecipeNotFoundException when updating non-existent recipe")
    void updateRecipe_notFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.updateRecipe(99L, request))
                .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete recipe successfully")
    void deleteRecipe_success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(1L);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    @DisplayName("Should throw RecipeNotFoundException when deleting non-existent recipe")
    void deleteRecipe_notFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.deleteRecipe(99L))
                .isInstanceOf(RecipeNotFoundException.class);

        verify(recipeRepository, never()).delete(any(Recipe.class));
    }

    @Test
    @DisplayName("Should filter recipes using specification")
    void filterRecipes_success() {
        RecipeFilter filter = new RecipeFilter(true, null, null, null, null);

        when(recipeRepository.findAll(any(Specification.class))).thenReturn(List.of(recipe));
        when(recipeMapper.toResponseList(List.of(recipe))).thenReturn(List.of(response));

        List<RecipeResponse> result = recipeService.filterRecipes(filter);

        assertThat(result).hasSize(1);
        verify(recipeRepository).findAll(any(Specification.class));
    }
}
