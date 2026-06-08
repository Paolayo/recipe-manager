package com.abnamro.recipes.controller;

import com.abnamro.recipes.dto.IngredientResponse;
import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.exception.DuplicateRecipeException;
import com.abnamro.recipes.exception.RecipeNotFoundException;
import com.abnamro.recipes.model.Recipe;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.abnamro.recipes.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    private RecipeRequest validRequest;
    private RecipeResponse recipeResponse;
    private List<IngredientResponse> ingredients;

    @BeforeEach
    void setUp() {
        ingredients = List.of(new IngredientResponse("pasta", null), new IngredientResponse("tomato", null));
        validRequest = new RecipeRequest("Pasta", true, 2, ingredients, "Boil pasta and add sauce.");
        recipeResponse = new RecipeResponse(1L, null, "Pasta", true, 2, ingredients, "Boil pasta and add sauce.", null, null);
    }

    @Test
    @DisplayName("POST /api/v1/recipes - should create recipe and return 201")
    void createRecipe_returns201() throws Exception {
        when(recipeService.createRecipe(any())).thenReturn(recipeResponse);

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.vegetarian").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/recipes - should return 400 when name is blank")
    void createRecipe_invalidRequest_returns400() throws Exception {
        validRequest = new RecipeRequest("", true, 2, ingredients, "Boil pasta and add sauce.");

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/recipes - should return 400 when servings is 0")
    void createRecipe_zeroServings_returns400() throws Exception {
        validRequest = new RecipeRequest("Pasta", true, 0, ingredients, "Boil pasta and add sauce.");

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.servings").exists());
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{id} - should return recipe")
    void getRecipeById_returns200() throws Exception {
        when(recipeService.getRecipeById(1L)).thenReturn(recipeResponse);

        mockMvc.perform(get("/api/v1/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pasta"));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{id} - should return 404 when not found")
    void getRecipeById_notFound_returns404() throws Exception {
        when(recipeService.getRecipeById(99L)).thenThrow(new RecipeNotFoundException(99L));

        mockMvc.perform(get("/api/v1/recipes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/recipes - should return list of recipes")
    void getAllRecipes_returns200() throws Exception {
        when(recipeService.getAllRecipes()).thenReturn(List.of(recipeResponse));

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/recipes/{id} - should update recipe")
    void updateRecipe_returns200() throws Exception {
        when(recipeService.updateRecipe(eq(1L), any())).thenReturn(recipeResponse);

        mockMvc.perform(put("/api/v1/recipes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/recipes/{id} - should delete recipe and return 204")
    void deleteRecipe_returns204() throws Exception {
        doNothing().when(recipeService).deleteRecipe(1L);

        mockMvc.perform(delete("/api/v1/recipes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/recipes/{id} - should return 404 when not found")
    void deleteRecipe_notFound_returns404() throws Exception {
        doThrow(new RecipeNotFoundException(99L)).when(recipeService).deleteRecipe(99L);

        mockMvc.perform(delete("/api/v1/recipes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/recipes/search - should filter by vegetarian")
    void searchRecipes_vegetarianFilter() throws Exception {
        when(recipeService.filterRecipes(any())).thenReturn(List.of(recipeResponse));

        mockMvc.perform(get("/api/v1/recipes/search")
                        .param("vegetarian", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/search - should filter by servings and ingredient")
    void searchRecipes_servingsAndIngredient() throws Exception {
        when(recipeService.filterRecipes(any())).thenReturn(List.of(recipeResponse));

        mockMvc.perform(get("/api/v1/recipes/search")
                        .param("servings", "2")
                        .param("includeIngredients", "pasta"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/recipes - should return 409 when recipe name is duplicate")
    void createRecipe_duplicateName_returns409() throws Exception {
        when(recipeService.createRecipe(any())).thenThrow(new DuplicateRecipeException("Pasta"));

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Recipe"))
                .andExpect(jsonPath("$.detail").value("A recipe with the name 'Pasta' already exists"));
    }

    @Test
    @DisplayName("PUT /api/v1/recipes/{id} - should return 409 on optimistic locking conflict")
    void updateRecipe_optimisticLocking_returns409() throws Exception {
        when(recipeService.updateRecipe(eq(1L), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Recipe.class, 1L));

        mockMvc.perform(put("/api/v1/recipes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Concurrent Modification"));
    }

    @Test
    @DisplayName("POST /api/v1/recipes - should return 400 when request body is malformed JSON")
    void createRecipe_malformedBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{id} - should return 400 when id is not a number")
    void getRecipeById_typeMismatch_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Parameter"))
                .andExpect(jsonPath("$.detail").value("Parameter 'id' must be of type Long"));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{id} - should return 500 on unexpected error")
    void getRecipeById_unexpectedError_returns500() throws Exception {
        when(recipeService.getRecipeById(1L)).thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/api/v1/recipes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.exceptionType").value("java.lang.RuntimeException"));
    }
}
