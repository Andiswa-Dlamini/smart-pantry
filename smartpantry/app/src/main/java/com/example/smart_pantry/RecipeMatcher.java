package com.example.smart_pantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper utility class to match pantry inventory against recipes.
 * Handles ingredient name normalization, unit conversions, pantry stock aggregation,
 * and strict recipe matching logic.
 */
public class RecipeMatcher {

    /**
     * Helper class representing a quantity converted to a standardized base unit.
     */
    public static class NormalizedQuantity {
        public final double baseAmount;
        public final String baseCategory; // "weight", "volume", "count", or custom unit string

        public NormalizedQuantity(double baseAmount, String baseCategory) {
            this.baseAmount = baseAmount;
            this.baseCategory = baseCategory;
        }
    }

    /**
      Normalizes ingredient names for reliable comparison:
      - Trims leading and trailing whitespace
      - Converts text to lowercase
      - Collapses multiple spaces into a single space
      - Normalizes simple plurals (e.g. "tomatoes" -> "tomato", "eggs" -> "egg")
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) {
            return "";
        }

        // Trim, convert to lowercase, and collapse whitespace
        String name = rawName.trim().toLowerCase().replaceAll("\\s+", " ");

        // Plural normalization rules
        if (name.endsWith("tomatoes")) {
            name = name.substring(0, name.length() - 2); // tomato
        } else if (name.endsWith("potatoes")) {
            name = name.substring(0, name.length() - 2); // potato
        } else if (name.endsWith("berries")) {
            name = name.substring(0, name.length() - 3) + "y"; // berry
        } else if (name.endsWith("leaves")) {
            name = name.substring(0, name.length() - 3) + "f"; // leaf
        } else if (name.endsWith("s") && !name.endsWith("ss") && !name.endsWith("is") && name.length() > 3) {
            name = name.substring(0, name.length() - 1); // remove trailing 's'
        }

        return name;
    }

    /**
     * Converts a given quantity and unit string to a standardized base unit and category.
     * Categories:
     * - Weight: converted to grams (g)
     * - Volume: converted to milliliters (ml)
     * - Count: converted to pieces (pcs)
     */
    public static NormalizedQuantity convertToBaseUnit(double quantity, String unit) {
        if (unit == null) {
            unit = "";
        }
        String u = unit.trim().toLowerCase();

        // Weight units -> base unit: grams (g)
        if (u.equals("g") || u.equals("gram") || u.equals("grams")) {
            return new NormalizedQuantity(quantity, "weight");
        } else if (u.equals("kg") || u.equals("kilo") || u.equals("kilogram") || u.equals("kilograms")) {
            return new NormalizedQuantity(quantity * 1000.0, "weight");
        } else if (u.equals("mg") || u.equals("milligram") || u.equals("milligrams")) {
            return new NormalizedQuantity(quantity * 0.001, "weight");
        } else if (u.equals("oz") || u.equals("ounce") || u.equals("ounces")) {
            return new NormalizedQuantity(quantity * 28.3495, "weight");
        } else if (u.equals("lb") || u.equals("lbs") || u.equals("pound") || u.equals("pounds")) {
            return new NormalizedQuantity(quantity * 453.592, "weight");
        }

        // Volume units -> base unit: milliliters (ml)
        if (u.equals("ml") || u.equals("milliliter") || u.equals("milliliters") || u.equals("millilitre")) {
            return new NormalizedQuantity(quantity, "volume");
        } else if (u.equals("l") || u.equals("liter") || u.equals("liters") || u.equals("litre") || u.equals("litres")) {
            return new NormalizedQuantity(quantity * 1000.0, "volume");
        } else if (u.equals("tsp") || u.equals("teaspoon") || u.equals("teaspoons")) {
            return new NormalizedQuantity(quantity * 5.0, "volume");
        } else if (u.equals("tbsp") || u.equals("tablespoon") || u.equals("tablespoons")) {
            return new NormalizedQuantity(quantity * 15.0, "volume");
        } else if (u.equals("cup") || u.equals("cups")) {
            return new NormalizedQuantity(quantity * 240.0, "volume");
        }

        // Count units -> base unit: pieces (pcs)
        if (u.equals("pcs") || u.equals("piece") || u.equals("pieces") ||
            u.equals("item") || u.equals("items") || u.equals("head") || u.equals("heads") ||
            u.equals("clove") || u.equals("cloves") || u.equals("slice") || u.equals("slices") ||
            u.isEmpty()) {
            return new NormalizedQuantity(quantity, "count");
        }

        // Custom / Unrecognized unit: treat literal string as its category
        return new NormalizedQuantity(quantity, u);
    }

    /**
     * Builds a nested map representing available pantry stock:
     * Map<NormalizedName, Map<BaseCategory, TotalQuantityInBaseUnit>>
     * Automatically sums up duplicate pantry entries for the same ingredient.
     */
    public static Map<String, Map<String, Double>> buildPantryMap(ArrayList<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> pantryMap = new HashMap<>();

        if (pantryItems == null) {
            return pantryMap;
        }

        for (PantryItem item : pantryItems) {
            if (item == null || item.getItemName() == null) {
                continue;
            }

            String normName = normalizeName(item.getItemName());
            NormalizedQuantity normQty = convertToBaseUnit(item.getQuantity(), item.getUnit());

            Map<String, Double> categoryMap = pantryMap.get(normName);
            if (categoryMap == null) {
                categoryMap = new HashMap<>();
                pantryMap.put(normName, categoryMap);
            }

            double currentTotal = categoryMap.containsKey(normQty.baseCategory)
                    ? categoryMap.get(normQty.baseCategory)
                    : 0.0;

            categoryMap.put(normQty.baseCategory, currentTotal + normQty.baseAmount);
        }

        return pantryMap;
    }

    /**
     * Strict Matching: Returns ONLY recipes where EVERY required ingredient
     * is present in the pantry in at least the required quantity.
     * (4 out of 5 ingredients = EXCLUDED).
     */
    public static ArrayList<Recipe> getStrictMatchingRecipes(ArrayList<Recipe> allRecipes, ArrayList<PantryItem> pantryItems) {
        ArrayList<Recipe> matchedRecipes = new ArrayList<>();
        if (allRecipes == null) {
            return matchedRecipes;
        }

        Map<String, Map<String, Double>> pantryMap = buildPantryMap(pantryItems);

        for (Recipe recipe : allRecipes) {
            if (recipe == null || recipe.getIngredients() == null) {
                continue;
            }

            boolean allIngredientsSatisfied = true;

            for (RecipeIngredient reqIngredient : recipe.getIngredients()) {
                if (!isIngredientSatisfied(reqIngredient, pantryMap)) {
                    allIngredientsSatisfied = false;
                    break; // Strict rule: stop checking if any requirement fails
                }
            }

            if (allIngredientsSatisfied && !recipe.getIngredients().isEmpty()) {
                matchedRecipes.add(recipe);
            }
        }

        return matchedRecipes;
    }

    /**
     * Almost Matching (Bonus): Returns recipes missing EXACTLY ONE ingredient.
     * Kept completely separate from the strict list.
     */
    public static ArrayList<Recipe> getAlmostMatchingRecipes(ArrayList<Recipe> allRecipes, ArrayList<PantryItem> pantryItems) {
        ArrayList<Recipe> almostMatchedRecipes = new ArrayList<>();
        if (allRecipes == null) {
            return almostMatchedRecipes;
        }

        Map<String, Map<String, Double>> pantryMap = buildPantryMap(pantryItems);

        for (Recipe recipe : allRecipes) {
            if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
                continue;
            }

            int missingCount = 0;

            for (RecipeIngredient reqIngredient : recipe.getIngredients()) {
                if (!isIngredientSatisfied(reqIngredient, pantryMap)) {
                    missingCount++;
                }
            }

            // Exactly 1 ingredient missing
            if (missingCount == 1) {
                almostMatchedRecipes.add(recipe);
            }
        }

        return almostMatchedRecipes;
    }

    /**
     * Calculates and returns the list of missing or understocked ingredients for a given recipe
     * compared against current pantry inventory.
     */
    public static ArrayList<RecipeIngredient> getMissingIngredientsForRecipe(Recipe recipe, ArrayList<PantryItem> pantryItems) {
        ArrayList<RecipeIngredient> missingIngredients = new ArrayList<>();
        if (recipe == null || recipe.getIngredients() == null) {
            return missingIngredients;
        }

        Map<String, Map<String, Double>> pantryMap = buildPantryMap(pantryItems);

        for (RecipeIngredient reqIngredient : recipe.getIngredients()) {
            if (reqIngredient == null || reqIngredient.getIngredientName() == null) {
                continue;
            }

            String normReqName = normalizeName(reqIngredient.getIngredientName());
            NormalizedQuantity reqNormQty = convertToBaseUnit(reqIngredient.getRequiredQuantity(), reqIngredient.getUnit());

            Map<String, Double> categoryMap = pantryMap.get(normReqName);
            double availableBaseAmount = 0.0;
            if (categoryMap != null && categoryMap.containsKey(reqNormQty.baseCategory)) {
                availableBaseAmount = categoryMap.get(reqNormQty.baseCategory);
            }

            if (availableBaseAmount < reqNormQty.baseAmount) {
                double missingBaseAmount = reqNormQty.baseAmount - availableBaseAmount;

                RecipeIngredient missingIng = new RecipeIngredient();
                missingIng.setIngredientName(reqIngredient.getIngredientName());
                missingIng.setUnit(reqIngredient.getUnit());

                double missingRatio = missingBaseAmount / reqNormQty.baseAmount;
                double missingDisplayQty = Math.round((reqIngredient.getRequiredQuantity() * missingRatio) * 100.0) / 100.0;

                missingIng.setRequiredQuantity(missingDisplayQty > 0 ? missingDisplayQty : reqIngredient.getRequiredQuantity());
                missingIngredients.add(missingIng);
            }
        }

        return missingIngredients;
    }

    /**
     * Checks if a specific required recipe ingredient is satisfied by the pantry stock.
     */
    private static boolean isIngredientSatisfied(RecipeIngredient reqIngredient, Map<String, Map<String, Double>> pantryMap) {
        if (reqIngredient == null || reqIngredient.getIngredientName() == null) {
            return false;
        }

        String normReqName = normalizeName(reqIngredient.getIngredientName());
        NormalizedQuantity reqNormQty = convertToBaseUnit(reqIngredient.getRequiredQuantity(), reqIngredient.getUnit());

        Map<String, Double> categoryMap = pantryMap.get(normReqName);
        if (categoryMap == null) {
            return false; // Ingredient not in pantry
        }

        Double pantryBaseAmount = categoryMap.get(reqNormQty.baseCategory);
        if (pantryBaseAmount == null) {
            return false; // Unit mismatch (e.g. grams vs pieces)
        }

        return pantryBaseAmount >= reqNormQty.baseAmount;
    }
}
