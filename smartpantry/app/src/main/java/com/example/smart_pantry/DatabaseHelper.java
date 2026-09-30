package com.example.smart_pantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    // Table 1: Pantry
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "_id";
    public static final String COLUMN_PANTRY_NAME = "item_name";
    public static final String COLUMN_PANTRY_QUANTITY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";
    public static final String COLUMN_PANTRY_EXPIRY_DATE = "expiry_date";

    // Table 2: Recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_ID = "_id";
    public static final String COLUMN_RECIPE_NAME = "recipe_name";
    public static final String COLUMN_RECIPE_INSTRUCTIONS = "instructions";
    public static final String COLUMN_RECIPE_CATEGORY = "category";

    // Table 3: Recipe Ingredients
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COLUMN_INGREDIENT_ID = "_id";
    public static final String COLUMN_INGREDIENT_RECIPE_ID = "recipe_id";
    public static final String COLUMN_INGREDIENT_NAME = "ingredient_name";
    public static final String COLUMN_INGREDIENT_REQUIRED_QTY = "required_quantity";
    public static final String COLUMN_INGREDIENT_UNIT = "unit";

    // SQL statement to create the pantry table
    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " (" +
                    COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PANTRY_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PANTRY_QUANTITY + " REAL NOT NULL, " +
                    COLUMN_PANTRY_UNIT + " TEXT NOT NULL, " +
                    COLUMN_PANTRY_EXPIRY_DATE + " TEXT);";

    // SQL statement to create the recipes table
    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
                    COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                    COLUMN_RECIPE_INSTRUCTIONS + " TEXT NOT NULL, " +
                    COLUMN_RECIPE_CATEGORY + " TEXT);";

    // SQL statement to create the recipe ingredients junction table
    private static final String CREATE_TABLE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COLUMN_INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INGREDIENT_RECIPE_ID + " INTEGER NOT NULL, " +
                    COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
                    COLUMN_INGREDIENT_REQUIRED_QTY + " REAL NOT NULL, " +
                    COLUMN_INGREDIENT_UNIT + " TEXT NOT NULL, " +
                    "FOREIGN KEY (" + COLUMN_INGREDIENT_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + COLUMN_RECIPE_ID + ") ON DELETE CASCADE);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create database tables
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENTS);

        // Seed initial 18 recipes and required ingredients
        seedInitialRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // Helper method to insert a recipe and return its auto-generated ID
    private long insertRecipe(SQLiteDatabase db, String name, String instructions, String category) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RECIPE_NAME, name);
        cv.put(COLUMN_RECIPE_INSTRUCTIONS, instructions);
        cv.put(COLUMN_RECIPE_CATEGORY, category);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    // Helper method to insert a recipe ingredient requirement
    private void insertIngredient(SQLiteDatabase db, long recipeId, String ingredientName, double quantity, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INGREDIENT_RECIPE_ID, recipeId);
        cv.put(COLUMN_INGREDIENT_NAME, ingredientName);
        cv.put(COLUMN_INGREDIENT_REQUIRED_QTY, quantity);
        cv.put(COLUMN_INGREDIENT_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }

    // Seeds 18 diverse recipes with ingredients and quantities upon first database creation
    private void seedInitialRecipes(SQLiteDatabase db) {
        long id;

        // 1. Classic Omelette
        id = insertRecipe(db, "Classic Omelette", "1. Whisk eggs with salt. 2. Melt butter in pan. 3. Pour eggs, add cheese, fold and serve.", "Breakfast");
        insertIngredient(db, id, "Eggs", 2, "items");
        insertIngredient(db, id, "Butter", 10, "grams");
        insertIngredient(db, id, "Cheese", 50, "grams");
        insertIngredient(db, id, "Salt", 2, "grams");

        // 2. Fluffy Pancakes
        id = insertRecipe(db, "Fluffy Pancakes", "1. Mix flour, sugar, milk, and eggs. 2. Cook on greased skillet until golden brown on both sides.", "Breakfast");
        insertIngredient(db, id, "Flour", 200, "grams");
        insertIngredient(db, id, "Milk", 250, "ml");
        insertIngredient(db, id, "Eggs", 2, "items");
        insertIngredient(db, id, "Sugar", 30, "grams");
        insertIngredient(db, id, "Butter", 20, "grams");

        // 3. French Toast
        id = insertRecipe(db, "French Toast", "1. Dip bread slices into beaten egg and milk mixture. 2. Fry in butter until brown.", "Breakfast");
        insertIngredient(db, id, "Bread", 4, "items");
        insertIngredient(db, id, "Eggs", 2, "items");
        insertIngredient(db, id, "Milk", 100, "ml");
        insertIngredient(db, id, "Butter", 15, "grams");
        insertIngredient(db, id, "Sugar", 10, "grams");

        // 4. Banana Fruit Smoothie
        id = insertRecipe(db, "Banana Fruit Smoothie", "1. Blend bananas, milk, yogurt, and honey until smooth.", "Breakfast");
        insertIngredient(db, id, "Banana", 2, "items");
        insertIngredient(db, id, "Milk", 300, "ml");
        insertIngredient(db, id, "Yogurt", 150, "grams");
        insertIngredient(db, id, "Honey", 20, "grams");

        // 5. Honey Oatmeal Bowl
        id = insertRecipe(db, "Honey Oatmeal Bowl", "1. Cook oats in warm milk. 2. Top with sliced banana and drizzle honey.", "Breakfast");
        insertIngredient(db, id, "Oats", 100, "grams");
        insertIngredient(db, id, "Milk", 250, "ml");
        insertIngredient(db, id, "Honey", 15, "grams");
        insertIngredient(db, id, "Banana", 1, "items");

        // 6. Grilled Cheese Sandwich
        id = insertRecipe(db, "Grilled Cheese Sandwich", "1. Butter outer bread sides. Place cheese between bread and grill until melted.", "Lunch");
        insertIngredient(db, id, "Bread", 2, "items");
        insertIngredient(db, id, "Cheese", 60, "grams");
        insertIngredient(db, id, "Butter", 15, "grams");

        // 7. BLT Sandwich
        id = insertRecipe(db, "BLT Sandwich", "1. Fry bacon. Toast bread. Spread mayonnaise and layer bacon, lettuce, and tomato slices.", "Lunch");
        insertIngredient(db, id, "Bread", 2, "items");
        insertIngredient(db, id, "Bacon", 3, "items");
        insertIngredient(db, id, "Lettuce", 30, "grams");
        insertIngredient(db, id, "Tomato", 1, "items");
        insertIngredient(db, id, "Mayonnaise", 15, "grams");

        // 8. Chicken Salad
        id = insertRecipe(db, "Chicken Salad", "1. Toss cooked chicken breast, sliced lettuce, tomato, mayonnaise, and olive oil.", "Lunch");
        insertIngredient(db, id, "Chicken Breast", 200, "grams");
        insertIngredient(db, id, "Lettuce", 100, "grams");
        insertIngredient(db, id, "Tomato", 1, "items");
        insertIngredient(db, id, "Olive Oil", 15, "ml");
        insertIngredient(db, id, "Mayonnaise", 30, "grams");

        // 9. Creamy Tomato Soup
        id = insertRecipe(db, "Creamy Tomato Soup", "1. Sauté onion in butter. Add tomatoes and simmer. Blend smooth and stir in milk and salt.", "Lunch");
        insertIngredient(db, id, "Tomato", 4, "items");
        insertIngredient(db, id, "Onion", 1, "items");
        insertIngredient(db, id, "Butter", 20, "grams");
        insertIngredient(db, id, "Milk", 100, "ml");
        insertIngredient(db, id, "Salt", 5, "grams");

        // 10. Avocado Toast
        id = insertRecipe(db, "Avocado Toast", "1. Toast bread. Mash avocado with lemon juice and salt. Spread on toast and top with tomato.", "Lunch");
        insertIngredient(db, id, "Bread", 2, "items");
        insertIngredient(db, id, "Avocado", 1, "items");
        insertIngredient(db, id, "Salt", 2, "grams");
        insertIngredient(db, id, "Lemon Juice", 10, "ml");
        insertIngredient(db, id, "Tomato", 1, "items");

        // 11. Spaghetti Bolognese
        id = insertRecipe(db, "Spaghetti Bolognese", "1. Cook ground beef, garlic, onion in olive oil. Add tomato sauce. Serve over boiled spaghetti.", "Dinner");
        insertIngredient(db, id, "Spaghetti", 250, "grams");
        insertIngredient(db, id, "Ground Beef", 300, "grams");
        insertIngredient(db, id, "Tomato Sauce", 200, "ml");
        insertIngredient(db, id, "Onion", 1, "items");
        insertIngredient(db, id, "Garlic", 2, "items");
        insertIngredient(db, id, "Olive Oil", 15, "ml");

        // 12. Chicken Stir Fry
        id = insertRecipe(db, "Chicken Stir Fry", "1. Stir fry sliced chicken breast, broccoli, and garlic in vegetable oil. Add soy sauce and serve with cooked rice.", "Dinner");
        insertIngredient(db, id, "Chicken Breast", 300, "grams");
        insertIngredient(db, id, "Broccoli", 150, "grams");
        insertIngredient(db, id, "Soy Sauce", 30, "ml");
        insertIngredient(db, id, "Garlic", 2, "items");
        insertIngredient(db, id, "Rice", 200, "grams");
        insertIngredient(db, id, "Vegetable Oil", 15, "ml");

        // 13. Garlic Butter Rice
        id = insertRecipe(db, "Garlic Butter Rice", "1. Sauté minced garlic in butter. Add cooked rice and salt, tossing evenly.", "Dinner");
        insertIngredient(db, id, "Rice", 200, "grams");
        insertIngredient(db, id, "Butter", 30, "grams");
        insertIngredient(db, id, "Garlic", 3, "items");
        insertIngredient(db, id, "Salt", 3, "grams");

        // 14. Beef Tacos
        id = insertRecipe(db, "Beef Tacos", "1. Season and brown ground beef. Fill tortilla shells with beef, cheese, lettuce, and diced tomato.", "Dinner");
        insertIngredient(db, id, "Tortilla", 4, "items");
        insertIngredient(db, id, "Ground Beef", 250, "grams");
        insertIngredient(db, id, "Cheese", 80, "grams");
        insertIngredient(db, id, "Lettuce", 50, "grams");
        insertIngredient(db, id, "Tomato", 1, "items");

        // 15. Vegetable Curry
        id = insertRecipe(db, "Vegetable Curry", "1. Sauté onion, potatoes, and carrots. Stir in curry powder and coconut milk. Serve over rice.", "Dinner");
        insertIngredient(db, id, "Potato", 2, "items");
        insertIngredient(db, id, "Carrot", 2, "items");
        insertIngredient(db, id, "Onion", 1, "items");
        insertIngredient(db, id, "Curry Powder", 15, "grams");
        insertIngredient(db, id, "Coconut Milk", 200, "ml");
        insertIngredient(db, id, "Rice", 200, "grams");

        // 16. Macaroni and Cheese
        id = insertRecipe(db, "Macaroni and Cheese", "1. Make roux with butter, flour, and milk. Melt in cheese and fold in boiled pasta.", "Dinner");
        insertIngredient(db, id, "Pasta", 200, "grams");
        insertIngredient(db, id, "Cheese", 150, "grams");
        insertIngredient(db, id, "Milk", 150, "ml");
        insertIngredient(db, id, "Butter", 25, "grams");
        insertIngredient(db, id, "Flour", 15, "grams");

        // 17. Creamy Mashed Potatoes
        id = insertRecipe(db, "Creamy Mashed Potatoes", "1. Boil potatoes until tender. Mash with warm milk, butter, and salt.", "Dinner");
        insertIngredient(db, id, "Potato", 4, "items");
        insertIngredient(db, id, "Milk", 100, "ml");
        insertIngredient(db, id, "Butter", 30, "grams");
        insertIngredient(db, id, "Salt", 4, "grams");

        // 18. Scrambled Eggs on Toast
        id = insertRecipe(db, "Scrambled Eggs on Toast", "1. Scramble eggs softly in butter with salt. Serve hot over toasted bread.", "Breakfast");
        insertIngredient(db, id, "Bread", 2, "items");
        insertIngredient(db, id, "Eggs", 3, "items");
        insertIngredient(db, id, "Butter", 15, "grams");
        insertIngredient(db, id, "Salt", 2, "grams");
    }
}
