CREATE TABLE recipes (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    name        VARCHAR(255) NOT NULL,
    vegetarian  BOOLEAN NOT NULL DEFAULT FALSE,
    servings    INTEGER NOT NULL,
    instructions TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE recipe_ingredients (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    recipe_id   BIGINT NOT NULL,
    ingredient  VARCHAR(255) NOT NULL,
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipes_vegetarian ON recipes(vegetarian);
CREATE INDEX idx_recipes_servings ON recipes(servings);
CREATE INDEX idx_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id);
CREATE UNIQUE INDEX uq_recipe_ingredients ON recipe_ingredients(recipe_id, ingredient);