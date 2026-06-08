package com.abnamro.recipes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientTest {

    @Test
    @DisplayName("Builder sets name correctly")
    void builder_setsName() {
        Ingredient ingredient = Ingredient.builder().name("pasta").build();

        assertThat(ingredient.getName()).isEqualTo("pasta");
        assertThat(ingredient.getId()).isNull();
        assertThat(ingredient.getVersion()).isNull();
    }

    @Test
    @DisplayName("Ingredients with the same id are equal regardless of name")
    void equals_sameId_areEqual() {
        Ingredient a = Ingredient.builder().id(1L).name("pasta").build();
        Ingredient b = Ingredient.builder().id(1L).name("different").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("Ingredients with different ids are not equal")
    void equals_differentId_areNotEqual() {
        Ingredient a = Ingredient.builder().id(1L).name("pasta").build();
        Ingredient b = Ingredient.builder().id(2L).name("pasta").build();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("Unsaved ingredients (null id) are equal to each other")
    void equals_nullId_areEqual() {
        Ingredient a = Ingredient.builder().name("pasta").build();
        Ingredient b = Ingredient.builder().name("tomato").build();

        assertThat(a).isEqualTo(b);
    }
}