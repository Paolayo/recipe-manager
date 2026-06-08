package com.abnamro.recipes.mapper;

import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.model.Ingredient;
import com.abnamro.recipes.model.Recipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RecipeMapperTest {

    @Autowired
    private RecipeMapper recipeMapper;

    @Test
    @DisplayName("toResponse maps ingredient entities to name strings")
    void toResponse_mapsIngredients() {
        Recipe recipe = Recipe.builder()
                .name("Pasta")
                .vegetarian(true)
                .servings(2)
                .ingredients(List.of(
                        Ingredient.builder().name("pasta").build(),
                        Ingredient.builder().name("tomato").build()))
                .instructions("Boil pasta.")
                .build();

        RecipeResponse response = recipeMapper.toResponse(recipe);

        assertThat(response.ingredients()).containsExactly("pasta", "tomato");
    }

    @Test
    @DisplayName("toEntity maps ingredient name strings to Ingredient entities")
    void toEntity_mapsIngredients() {
        RecipeRequest request = new RecipeRequest(
                "Pasta", true, 2, List.of("pasta", "tomato"), "Boil pasta.");

        Recipe recipe = recipeMapper.toEntity(request);

        assertThat(recipe.getIngredients())
                .extracting(Ingredient::getName)
                .containsExactly("pasta", "tomato");
    }

    @Test
    @DisplayName("toStringList returns empty list when ingredients are null")
    void toStringList_null_returnsEmptyList() {
        assertThat(recipeMapper.toStringList(null)).isEmpty();
    }

    @Test
    @DisplayName("toIngredientList returns empty list when names are null")
    void toIngredientList_null_returnsEmptyList() {
        assertThat(recipeMapper.toIngredientList(null)).isEmpty();
    }

    @Test
    @DisplayName("updateEntityFromRequest does not overwrite id or createdAt")
    void updateEntityFromRequest_preservesAuditFields() {
        LocalDateTime originalCreatedAt = LocalDateTime.of(2024, Month.JANUARY, 1, 12, 0);
        Recipe existing = Recipe.builder()
                .name("Old Name")
                .vegetarian(false)
                .servings(2)
                .instructions("Old instructions.")
                .build();
        existing.setId(42L);
        existing.setCreatedAt(originalCreatedAt);

        RecipeRequest update = new RecipeRequest(
                "New Name", true, 4, List.of("pasta"), "New instructions.");

        recipeMapper.updateEntityFromRequest(update, existing);

        assertThat(existing.getId()).isEqualTo(42L);
        assertThat(existing.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getServings()).isEqualTo(4);
    }
}