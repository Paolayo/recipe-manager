package com.abnamro.recipes.service;

import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.dto.RecipeRequest;
import com.abnamro.recipes.dto.RecipeResponse;
import com.abnamro.recipes.exception.DuplicateRecipeException;
import com.abnamro.recipes.exception.RecipeNotFoundException;
import com.abnamro.recipes.mapper.RecipeMapper;
import com.abnamro.recipes.model.Recipe;
import com.abnamro.recipes.repository.RecipeRepository;
import com.abnamro.recipes.specification.RecipeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transactional implementation of {@link RecipeService}.
 * All read operations run in a read-only transaction; write operations override with a full transaction.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request) {
        if (recipeRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateRecipeException(request.name());
        }
        return recipeMapper.toResponse(recipeRepository.save(recipeMapper.toEntity(request)));
    }

    @Override
    public RecipeResponse getRecipeById(Long id) {
        return recipeMapper.toResponse(
                recipeRepository.findById(id).orElseThrow(() -> new RecipeNotFoundException(id))
        );
    }

    @Override
    public List<RecipeResponse> getAllRecipes() {
        return recipeMapper.toResponseList(recipeRepository.findAll());
    }

    @Override
    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        if (recipeRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new DuplicateRecipeException(request.name());
        }
        recipe.getIngredients().clear();
        recipeRepository.flush();
        recipeMapper.updateEntityFromRequest(request, recipe);
        return recipeMapper.toResponse(recipeRepository.save(recipe));
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        recipeRepository.delete(recipe);
    }

    @Override
    public List<RecipeResponse> filterRecipes(RecipeFilter filter) {
        return recipeMapper.toResponseList(recipeRepository.findAll(RecipeSpecification.withFilter(filter)));
    }
}