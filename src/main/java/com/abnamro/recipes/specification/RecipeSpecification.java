package com.abnamro.recipes.specification;

import com.abnamro.recipes.dto.RecipeFilter;
import com.abnamro.recipes.model.Ingredient;
import com.abnamro.recipes.model.Ingredient_;
import com.abnamro.recipes.model.Recipe;
import com.abnamro.recipes.model.Recipe_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA {@link Specification} factory that builds a compound predicate from a {@link com.abnamro.recipes.dto.RecipeFilter}.
 * Each non-null field in the filter contributes one AND predicate.
 * Ingredient inclusion uses a correlated EXISTS subquery; exclusion uses a NOT IN subquery
 * to avoid row duplication caused by the ingredient join.
 */
public class RecipeSpecification {

    private static final char ESCAPE_CHAR = '\\';

    private RecipeSpecification() {}

    public static Specification<Recipe> withFilter(RecipeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            vegetarianPredicate(filter, root, cb).ifPresent(predicates::add);
            servingsPredicate(filter, root, cb).ifPresent(predicates::add);
            predicates.addAll(includeIngredientPredicates(filter, root, query, cb));
            predicates.addAll(excludeIngredientPredicates(filter, root, query, cb));
            instructionSearchPredicate(filter, root, cb).ifPresent(predicates::add);

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Optional<Predicate> vegetarianPredicate(RecipeFilter filter, Root<Recipe> root, CriteriaBuilder cb) {
        if (filter.vegetarian() == null) return Optional.empty();
        return Optional.of(cb.equal(root.get(Recipe_.vegetarian), filter.vegetarian()));
    }

    private static Optional<Predicate> servingsPredicate(RecipeFilter filter, Root<Recipe> root, CriteriaBuilder cb) {
        if (filter.servings() == null) return Optional.empty();
        return Optional.of(cb.equal(root.get(Recipe_.servings), filter.servings()));
    }

    private static List<Predicate> includeIngredientPredicates(RecipeFilter filter, Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (filter.includeIngredients() == null || filter.includeIngredients().isEmpty()) return List.of();
        return filter.includeIngredients().stream()
                .map(ingredient -> {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Recipe> subRoot = subquery.from(Recipe.class);
                    Join<Recipe, Ingredient> ingredientJoin = subRoot.join(Recipe_.ingredients);
                    subquery.select(subRoot.get(Recipe_.id))
                            .where(
                                    cb.equal(subRoot.get(Recipe_.id), root.get(Recipe_.id)),
                                    cb.like(cb.lower(ingredientJoin.get(Ingredient_.name)), containsIgnoreCase(ingredient), ESCAPE_CHAR)
                            );
                    return cb.exists(subquery);
                })
                .toList();
    }

    private static List<Predicate> excludeIngredientPredicates(RecipeFilter filter, Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (filter.excludeIngredients() == null || filter.excludeIngredients().isEmpty()) return List.of();
        return filter.excludeIngredients().stream()
                .map(ingredient -> {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Recipe> subRoot = subquery.from(Recipe.class);
                    Join<Recipe, Ingredient> ingredientJoin = subRoot.join(Recipe_.ingredients);
                    subquery.select(subRoot.get(Recipe_.id))
                            .where(cb.like(cb.lower(ingredientJoin.get(Ingredient_.name)), containsIgnoreCase(ingredient), ESCAPE_CHAR));
                    return cb.not(root.get(Recipe_.id).in(subquery));
                })
                .toList();
    }

    private static Optional<Predicate> instructionSearchPredicate(RecipeFilter filter, Root<Recipe> root, CriteriaBuilder cb) {
        if (filter.instructionSearch() == null || filter.instructionSearch().isBlank()) return Optional.empty();
        return Optional.of(cb.like(
                cb.lower(root.get(Recipe_.instructions)),
                containsIgnoreCase(filter.instructionSearch()),
                ESCAPE_CHAR
        ));
    }

    private static String containsIgnoreCase(String value) {
        String escaped = value.toLowerCase()
                .replace(String.valueOf(ESCAPE_CHAR), ESCAPE_CHAR + String.valueOf(ESCAPE_CHAR))
                .replace("%", ESCAPE_CHAR + "%")
                .replace("_", ESCAPE_CHAR + "_");
        return "%" + escaped + "%";
    }
}