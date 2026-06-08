package com.abnamro.recipes.integration;

import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Recipe API Integration Tests")
class RecipeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
    }

    private RecipeRequest buildRequest(String name, boolean vegetarian, int servings,
                                       List<String> ingredients, String instructions) {
        return new RecipeRequest(name, vegetarian, servings, ingredients, instructions);
    }

    @Test
    @DisplayName("Full CRUD lifecycle: create, read, update, delete")
    void fullCrudLifecycle() throws Exception {
        // CREATE
        RecipeRequest createRequest = buildRequest(
                "Tomato Soup", true, 2,
                List.of("tomatoes", "onion", "garlic"),
                "Sauté onion and garlic. Add tomatoes. Simmer for 20 minutes. Blend."
        );

        String createResponse = mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Tomato Soup"))
                .andExpect(jsonPath("$.vegetarian").value(true))
                .andExpect(jsonPath("$.servings").value(2))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // READ by ID
        mockMvc.perform(get("/api/v1/recipes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato Soup"));

        // UPDATE
        RecipeRequest updateRequest = buildRequest(
                "Tomato Bisque", true, 4,
                List.of("tomatoes", "cream", "onion"),
                "Sauté onion. Add tomatoes. Simmer. Add cream. Blend until smooth."
        );

        mockMvc.perform(put("/api/v1/recipes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato Bisque"))
                .andExpect(jsonPath("$.servings").value(4));

        // DELETE
        mockMvc.perform(delete("/api/v1/recipes/" + id))
                .andExpect(status().isNoContent());

        // VERIFY deletion
        mockMvc.perform(get("/api/v1/recipes/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Filter: all vegetarian recipes")
    void filter_allVegetarian() throws Exception {
        seedRecipes();

        mockMvc.perform(get("/api/v1/recipes/search").param("vegetarian", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].vegetarian", everyItem(is(true))));
    }

    @Test
    @DisplayName("Filter: 4 servings with potatoes as ingredient")
    void filter_servingsAndIncludeIngredient() throws Exception {
        seedRecipes();

        mockMvc.perform(get("/api/v1/recipes/search")
                        .param("servings", "4")
                        .param("includeIngredients", "potatoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Potato Gratin"));
    }

    @Test
    @DisplayName("Filter: exclude salmon, instructions contain oven")
    void filter_excludeIngredientAndInstructionText() throws Exception {
        seedRecipes();

        mockMvc.perform(get("/api/v1/recipes/search")
                        .param("excludeIngredients", "salmon")
                        .param("instructionSearch", "oven"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItem(containsString("Salmon")))));
    }

    @Test
    @DisplayName("GET /recipes/{id} - returns 404 for unknown id")
    void getById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recipe Not Found"));
    }

    @Test
    @DisplayName("POST /recipes - returns 400 for invalid payload")
    void create_invalidPayload_returns400() throws Exception {
        RecipeRequest invalid = new RecipeRequest(null, null, 0, null, null);

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors").exists());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void seedRecipes() throws Exception {
        post_recipe("Potato Gratin", true, 4,
                List.of("potatoes", "cream", "gruyere"),
                "Layer sliced potatoes with cream. Bake in oven at 180°C for 45 minutes.");

        post_recipe("Pasta Primavera", true, 2,
                List.of("pasta", "zucchini", "tomatoes"),
                "Cook pasta al dente. Sauté vegetables. Combine and serve.");

        post_recipe("Baked Salmon", false, 2,
                List.of("salmon", "lemon", "dill"),
                "Place salmon on a baking tray. Season and cook in oven at 200°C for 20 minutes.");
    }

    private void post_recipe(String name, boolean vegetarian, int servings,
                              List<String> ingredients, String instructions) throws Exception {
        mockMvc.perform(post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(name, vegetarian, servings, ingredients, instructions)
                )))
                .andExpect(status().isCreated());
    }
}
