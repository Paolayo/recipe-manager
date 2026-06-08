package com.abnamro.recipes.mapper;

import com.abnamro.recipes.dto.IngredientResponse;
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

    @Mapping(target = "ingredients", qualifiedByName = "toIngredientResponseList")
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

    @Named("toIngredientResponseList")
    default List<IngredientResponse> toIngredientResponseList(List<Ingredient> ingredients) {
        if (ingredients == null) return List.of();
        return ingredients.stream()
                .map(i -> new IngredientResponse(i.getName(), i.getAmount()))
                .toList();
    }

    /** Wraps each {@link IngredientResponse} DTO into an {@link Ingredient} entity; IDs are left unset and assigned on persist. */
    @Named("toIngredientList")
    default List<Ingredient> toIngredientList(List<IngredientResponse> dtos) {
        if (dtos == null) return new ArrayList<>();
        return dtos.stream()
                .map(dto -> Ingredient.builder().name(dto.name()).amount(dto.amount()).build())
                .toList();
    }
}