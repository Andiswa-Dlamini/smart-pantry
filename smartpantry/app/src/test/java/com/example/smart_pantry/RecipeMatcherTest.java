package com.example.smart_pantry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Local unit test for RecipeMatcher verification.
 * Verifies strict matching, plural normalization, unit conversions, and almost-matching logic.
 */
public class RecipeMatcherTest {

    private ArrayList<Recipe> allRecipes;
    private ArrayList<PantryItem> pantryItems;

    @Before
    public void setUp() {
        allRecipes = new ArrayList<>();
        pantryItems = new ArrayList<>();
    }

    /**
     * Test Case 1: Exact Match
     * Pantry has all required ingredients in required quantities. Recipe MUST be matched.
     */
    @Test
    public void testExactMatch() {
        Recipe recipe = createRecipe("Pancake",
                createIngredient("Eggs", 2, "items"),
                createIngredient("Flour", 200, "g"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("Eggs", 2, "items"));
        pantryItems.add(createPantryItem("Flour", 200, "g"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertEquals(1, matched.size());
        assertEquals("Pancake", matched.get(0).getRecipeName());
    }

    /**
     * Test Case 2: 4 out of 5 ingredients present (EXCLUDED)
     * Recipe has 5 ingredients. Pantry has 4. Strict match MUST exclude it.
     * "Almost There" list MUST include it.
     */
    @Test
    public void testFourOfFiveExcluded() {
        Recipe recipe = createRecipe("Complex Cake",
                createIngredient("Eggs", 2, "items"),
                createIngredient("Flour", 200, "g"),
                createIngredient("Milk", 100, "ml"),
                createIngredient("Butter", 20, "g"),
                createIngredient("Sugar", 30, "g"));
        allRecipes.add(recipe);

        // Pantry only has 4 of 5 required ingredients (missing Sugar)
        pantryItems.add(createPantryItem("Eggs", 2, "items"));
        pantryItems.add(createPantryItem("Flour", 200, "g"));
        pantryItems.add(createPantryItem("Milk", 100, "ml"));
        pantryItems.add(createPantryItem("Butter", 20, "g"));

        ArrayList<Recipe> strictMatched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertTrue(strictMatched.isEmpty());

        ArrayList<Recipe> almostMatched = RecipeMatcher.getAlmostMatchingRecipes(allRecipes, pantryItems);
        assertEquals(1, almostMatched.size());
        assertEquals("Complex Cake", almostMatched.get(0).getRecipeName());
    }

    /**
     * Test Case 3: Plural vs Singular Normalization
     * Recipe requires "Tomatoes", pantry has "tomato". Should normalize and match.
     */
    @Test
    public void testTomatoVsTomatoes() {
        Recipe recipe = createRecipe("Tomato Salad",
                createIngredient("Tomatoes", 2, "items"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("tomato", 2, "items"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertEquals(1, matched.size());
        assertEquals("Tomato Salad", matched.get(0).getRecipeName());
    }

    /**
     * Test Case 4: Unit Conversion (g vs kg)
     * Recipe requires 500g Flour, pantry has 1kg Flour (1000g >= 500g). Must match.
     */
    @Test
    public void testUnitConversion() {
        Recipe recipe = createRecipe("Bread",
                createIngredient("Flour", 500, "g"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("Flour", 1, "kg"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertEquals(1, matched.size());
        assertEquals("Bread", matched.get(0).getRecipeName());
    }

    /**
     * Test Case 5: Insufficient Quantity
     * Recipe requires 300g Sugar, pantry only has 200g Sugar. Must be excluded.
     */
    @Test
    public void testNotEnoughQuantity() {
        Recipe recipe = createRecipe("Sweet Cake",
                createIngredient("Sugar", 300, "g"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("Sugar", 200, "g"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertTrue(matched.isEmpty());
    }

    /**
     * Test Case 6: Incompatible Units
     * Recipe requires 100g Flour (weight), pantry has 100 pcs Flour (count). Must be excluded.
     */
    @Test
    public void testIncompatibleUnits() {
        Recipe recipe = createRecipe("Cake",
                createIngredient("Flour", 100, "g"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("Flour", 100, "pcs"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertTrue(matched.isEmpty());
    }

    /**
     * Test Case 7: Aggregating Duplicate Pantry Entries
     * Pantry has two separate entries for "Egg": 1 Egg and 2 Eggs. Total = 3 Eggs.
     * Recipe requires 3 Eggs. Must match.
     */
    @Test
    public void testPantryQuantityAggregation() {
        Recipe recipe = createRecipe("Omelette",
                createIngredient("Egg", 3, "pcs"));
        allRecipes.add(recipe);

        pantryItems.add(createPantryItem("Egg", 1, "pcs"));
        pantryItems.add(createPantryItem("Egg", 2, "pcs"));

        ArrayList<Recipe> matched = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        assertEquals(1, matched.size());
        assertEquals("Omelette", matched.get(0).getRecipeName());
    }

    /**
     * Test Case 8: Missing Ingredients Calculation
     * Recipe requires 200g Flour and 50g Sugar.
     * Pantry has 100g Flour and 0g Sugar.
     * Expected missing ingredients: 100g Flour and 50g Sugar.
     */
    @Test
    public void testMissingIngredientsCalculation() {
        Recipe recipe = createRecipe("Special Cake",
                createIngredient("Flour", 200, "g"),
                createIngredient("Sugar", 50, "g"));

        pantryItems.add(createPantryItem("Flour", 100, "g"));

        ArrayList<RecipeIngredient> missing = RecipeMatcher.getMissingIngredientsForRecipe(recipe, pantryItems);
        assertEquals(2, missing.size());
        assertEquals("Flour", missing.get(0).getIngredientName());
        assertEquals(100.0, missing.get(0).getRequiredQuantity(), 0.01);
        assertEquals("Sugar", missing.get(1).getIngredientName());
        assertEquals(50.0, missing.get(1).getRequiredQuantity(), 0.01);
    }

    // Helper methods to build test objects cleanly
    private Recipe createRecipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe();
        recipe.setRecipeName(name);
        ArrayList<RecipeIngredient> list = new ArrayList<>();
        for (RecipeIngredient ing : ingredients) {
            list.add(ing);
        }
        recipe.setIngredients(list);
        return recipe;
    }

    private RecipeIngredient createIngredient(String name, double qty, String unit) {
        RecipeIngredient ing = new RecipeIngredient();
        ing.setIngredientName(name);
        ing.setRequiredQuantity(qty);
        ing.setUnit(unit);
        return ing;
    }

    private PantryItem createPantryItem(String name, double qty, String unit) {
        PantryItem item = new PantryItem();
        item.setItemName(name);
        item.setQuantity(qty);
        item.setUnit(unit);
        return item;
    }
}
