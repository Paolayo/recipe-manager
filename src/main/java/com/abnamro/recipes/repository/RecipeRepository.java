package com.abnamro.recipes.repository;

import com.abnamro.recipes.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data repository for {@link Recipe} entities.
 * Extends {@link JpaSpecificationExecutor} to support dynamic filtering via
 * {@link com.abnamro.recipes.specification.RecipeSpecification}.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    /** Returns true if any recipe has the given name, case-insensitively. */
    boolean existsByNameIgnoreCase(String name);

    /** Returns true if any recipe other than {@code excludedId} has the given name, case-insensitively. */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long excludedId);
}
