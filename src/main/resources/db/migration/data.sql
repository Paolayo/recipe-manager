INSERT INTO recipes (name, vegetarian, servings, instructions) VALUES
('Margherita Pizza', true, 4, 'Preheat oven to 220°C. Stretch dough on a floured surface. Spread tomato sauce. Add mozzarella. Bake for 15 minutes until golden.'),
('Grilled Salmon', false, 2, 'Season salmon with salt and pepper. Heat grill to medium-high. Grill salmon 4 minutes per side. Serve with lemon wedges.'),
('Potato Soup', true, 4, 'Dice potatoes and onions. Sauté onions in butter. Add potatoes and broth. Simmer for 20 minutes. Blend until smooth. Season to taste.'),
('Beef Stew', false, 6, 'Brown beef chunks in a Dutch oven. Add carrots, potatoes and onions. Pour in beef broth. Cover and cook in oven at 160°C for 2 hours.');

INSERT INTO recipe_ingredients (recipe_id, ingredient) VALUES
(1, 'pizza dough'),
(1, 'tomato sauce'),
(1, 'mozzarella'),
(1, 'fresh basil'),
(2, 'salmon fillet'),
(2, 'lemon'),
(2, 'olive oil'),
(2, 'salt'),
(3, 'potatoes'),
(3, 'onion'),
(3, 'butter'),
(3, 'vegetable broth'),
(4, 'beef'),
(4, 'potatoes'),
(4, 'carrots'),
(4, 'onion'),
(4, 'beef broth');