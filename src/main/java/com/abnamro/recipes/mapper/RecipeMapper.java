package com.abnamro.recipes.mapper;

import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.model.Ingredient;
import com.abnamro.recipes.model.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

/**
 * MapStruct mapper between {@link Recipe}/{@link Ingredient} domain objects and their DTO counterparts.
 */
@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(target = "ingredients", qualifiedByName = "toStringList")
    RecipeResponse toResponse(Recipe recipe);

    List<RecipeResponse> toResponseList(List<Recipe> recipes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ingredients", qualifiedByName = "toIngredientList")
    Recipe toEntity(RecipeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ingredients", qualifiedByName = "toIngredientList")
    void updateEntityFromRequest(RecipeRequest request, @MappingTarget Recipe recipe);

    /** Extracts the name from each {@link Ingredient} to produce a plain string list for the response DTO. */
    @Named("toStringList")
    default List<String> toStringList(List<Ingredient> ingredients) {
        if (ingredients == null) return List.of();
        return ingredients.stream().map(Ingredient::getName).toList();
    }

    /** Wraps each ingredient name string into an {@link Ingredient} entity; IDs are left unset and assigned on persist. */
    @Named("toIngredientList")
    default List<Ingredient> toIngredientList(List<String> names) {
        if (names == null) return new ArrayList<>();
        return names.stream()
                .map(name -> Ingredient.builder().name(name).build())
                .toList();
    }
}