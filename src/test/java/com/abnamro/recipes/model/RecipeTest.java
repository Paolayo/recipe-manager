package com.abnamro.recipes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeTest {

    @Test
    @DisplayName("Builder default ingredients list is mutable")
    void builder_defaultIngredients_isMutable() {
        Recipe recipe = Recipe.builder().name("Pasta").vegetarian(true).servings(2).instructions("Boil.").build();

        assertThat(recipe.getIngredients()).isInstanceOf(ArrayList.class);
        recipe.getIngredients().add(Ingredient.builder().name("pasta").build());
        assertThat(recipe.getIngredients()).hasSize(1);
    }

    @Test
    @DisplayName("setIngredients clears existing items and adds the new ones")
    void setIngredients_replacesContents() {
        Recipe recipe = Recipe.builder().name("Pasta").vegetarian(true).servings(2).instructions("Boil.")
                .ingredients(new ArrayList<>(List.of(Ingredient.builder().name("old").build())))
                .build();
        List<Ingredient> original = recipe.getIngredients();

        recipe.setIngredients(List.of(Ingredient.builder().name("pasta").build(),
                Ingredient.builder().name("tomato").build()));

        assertThat(recipe.getIngredients()).isSameAs(original);
        assertThat(recipe.getIngredients()).extracting(Ingredient::getName).containsExactly("pasta", "tomato");
    }

    @Test
    @DisplayName("setIngredients with null results in an empty collection")
    void setIngredients_null_clearsCollection() {
        Recipe recipe = Recipe.builder().name("Pasta").vegetarian(true).servings(2).instructions("Boil.")
                .ingredients(new ArrayList<>(List.of(Ingredient.builder().name("pasta").build())))
                .build();

        recipe.setIngredients(null);

        assertThat(recipe.getIngredients()).isEmpty();
    }

    @Test
    @DisplayName("Recipes with the same id are equal regardless of other fields")
    void equals_sameId_areEqual() {
        Recipe a = Recipe.builder().id(1L).name("Pasta").vegetarian(true).servings(2).instructions("Boil.").build();
        Recipe b = Recipe.builder().id(1L).name("Different Name").vegetarian(false).servings(4).instructions("Other.").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("Recipes with different ids are not equal")
    void equals_differentId_areNotEqual() {
        Recipe a = Recipe.builder().id(1L).name("Pasta").vegetarian(true).servings(2).instructions("Boil.").build();
        Recipe b = Recipe.builder().id(2L).name("Pasta").vegetarian(true).servings(2).instructions("Boil.").build();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("toString does not include ingredients to avoid lazy-load issues")
    void toString_excludesIngredients() {
        Recipe recipe = Recipe.builder().name("Pasta").vegetarian(true).servings(2).instructions("Boil.")
                .ingredients(new ArrayList<>(List.of(Ingredient.builder().name("pasta").build())))
                .build();

        assertThat(recipe.toString()).doesNotContain("ingredients");
    }
}