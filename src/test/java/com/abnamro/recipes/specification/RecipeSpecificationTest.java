package com.abnamro.recipes.specification;

import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.model.Ingredient;
import com.abnamro.recipes.model.Recipe;
import com.abnamro.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RecipeSpecificationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();

        Recipe vegetarianPotato = Recipe.builder()
                .name("Potato Soup")
                .vegetarian(true)
                .servings(4)
                .ingredients(List.of(
                        Ingredient.builder().name("potatoes").build(),
                        Ingredient.builder().name("onion").build(),
                        Ingredient.builder().name("broth").build()))
                .instructions("Boil potatoes and blend with onion.")
                .build();

        Recipe salmonOven = Recipe.builder()
                .name("Baked Salmon")
                .vegetarian(false)
                .servings(2)
                .ingredients(List.of(
                        Ingredient.builder().name("salmon").build(),
                        Ingredient.builder().name("lemon").build(),
                        Ingredient.builder().name("herbs").build()))
                .instructions("Season salmon and bake in oven at 200°C for 20 minutes.")
                .build();

        Recipe vegetarianPasta = Recipe.builder()
                .name("Pasta Primavera")
                .vegetarian(true)
                .servings(4)
                .ingredients(List.of(
                        Ingredient.builder().name("pasta").build(),
                        Ingredient.builder().name("zucchini").build(),
                        Ingredient.builder().name("tomato").build()))
                .instructions("Cook pasta. Sauté vegetables. Combine and serve.")
                .build();

        recipeRepository.saveAll(List.of(vegetarianPotato, salmonOven, vegetarianPasta));
    }

    @Test
    @DisplayName("Should return only vegetarian recipes")
    void filter_vegetarianOnly() {
        RecipeFilter filter = new RecipeFilter(true, null, null, null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(2).allMatch(Recipe::isVegetarian);
    }

    @Test
    @DisplayName("Should return recipes with 4 servings that include potatoes")
    void filter_servingsAndIngredient() {
        RecipeFilter filter = new RecipeFilter(null, 4, List.of("potatoes"), null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Potato Soup");
    }

    @Test
    @DisplayName("Should return recipes without salmon that have oven in instructions")
    void filter_excludeIngredientAndInstructionSearch() {
        Recipe potatoGratin = Recipe.builder()
                .name("Potato Gratin")
                .vegetarian(true)
                .servings(4)
                .ingredients(List.of(
                        Ingredient.builder().name("potatoes").build(),
                        Ingredient.builder().name("cream").build()))
                .instructions("Layer sliced potatoes with cream. Bake in oven at 180°C for 45 minutes.")
                .build();
        recipeRepository.save(potatoGratin);

        RecipeFilter filter = new RecipeFilter(null, null, null, List.of("salmon"), "oven");

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Potato Gratin");
    }

    @Test
    @DisplayName("Should return all recipes when includeIngredients list is empty")
    void filter_emptyIncludeIngredients_returnsAll() {
        RecipeFilter filter = new RecipeFilter(null, null, List.of(), null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should return all recipes when excludeIngredients list is empty")
    void filter_emptyExcludeIngredients_returnsAll() {
        RecipeFilter filter = new RecipeFilter(null, null, null, List.of(), null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should return all recipes when no filter is applied")
    void filter_noFilter_returnsAll() {
        RecipeFilter filter = new RecipeFilter(null, null, null, null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should return non-vegetarian recipes")
    void filter_nonVegetarian() {
        RecipeFilter filter = new RecipeFilter(false, null, null, null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Baked Salmon");
    }

    @Test
    @DisplayName("Should return empty list when no recipe matches the filter")
    void filter_noMatch_returnsEmpty() {
        RecipeFilter filter = new RecipeFilter(null, 99, null, null, null);

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should search by instruction text case-insensitively")
    void filter_instructionSearchCaseInsensitive() {
        RecipeFilter filter = new RecipeFilter(null, null, null, null, "BOIL");

        List<Recipe> results = recipeRepository.findAll(RecipeSpecification.withFilter(filter));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Potato Soup");
    }
}
