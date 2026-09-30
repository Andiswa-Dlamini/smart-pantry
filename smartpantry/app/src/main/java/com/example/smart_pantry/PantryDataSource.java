package com.example.smart_pantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class PantryDataSource {

    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public PantryDataSource(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // Get all pantry items, ordered alphabetically.
    public ArrayList<PantryItem> getPantryItems() {

        ArrayList<PantryItem> pantryItems = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_PANTRY_NAME + " ASC"
        );

        try {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PANTRY_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PANTRY_NAME);
            int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PANTRY_QUANTITY);
            int unitIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PANTRY_UNIT);
            int expiryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE);

            while (cursor.moveToNext()) {

                PantryItem item = new PantryItem();

                item.setPantryId(cursor.getInt(idIndex));
                item.setItemName(cursor.getString(nameIndex));
                item.setQuantity(cursor.getDouble(quantityIndex));
                item.setUnit(cursor.getString(unitIndex));

                if (expiryIndex >= 0 && !cursor.isNull(expiryIndex)) {
                    item.setExpiryDate(cursor.getString(expiryIndex));
                }

                pantryItems.add(item);
            }

        } finally {
            cursor.close();
        }

        return pantryItems;
    }

    // Insert a new pantry item.
    public boolean insertPantryItem(PantryItem item) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseHelper.COLUMN_PANTRY_NAME,
                item.getItemName().trim()
        );

        values.put(
                DatabaseHelper.COLUMN_PANTRY_QUANTITY,
                item.getQuantity()
        );

        values.put(
                DatabaseHelper.COLUMN_PANTRY_UNIT,
                item.getUnit()
        );

        if (item.getExpiryDate() == null ||
                item.getExpiryDate().trim().isEmpty()) {

            values.putNull(DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE);

        } else {

            values.put(
                    DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE,
                    item.getExpiryDate()
            );
        }

        return database.insert(
                DatabaseHelper.TABLE_PANTRY,
                null,
                values
        ) != -1;
    }

    // Update an existing pantry item.
    public boolean updatePantryItem(PantryItem item) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseHelper.COLUMN_PANTRY_NAME,
                item.getItemName().trim()
        );

        values.put(
                DatabaseHelper.COLUMN_PANTRY_QUANTITY,
                item.getQuantity()
        );

        values.put(
                DatabaseHelper.COLUMN_PANTRY_UNIT,
                item.getUnit()
        );

        if (item.getExpiryDate() == null ||
                item.getExpiryDate().trim().isEmpty()) {

            values.putNull(DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE);

        } else {

            values.put(
                    DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE,
                    item.getExpiryDate()
            );
        }

        int rowsUpdated = database.update(
                DatabaseHelper.TABLE_PANTRY,
                values,
                DatabaseHelper.COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(item.getPantryId())}
        );

        return rowsUpdated > 0;
    }

    // Delete a pantry item by ID.
    public boolean deletePantryItem(int pantryId) {

        int rowsDeleted = database.delete(
                DatabaseHelper.TABLE_PANTRY,
                DatabaseHelper.COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(pantryId)}
        );

        return rowsDeleted > 0;
    }

    // Retrieve one pantry item by ID.
    public PantryItem getSpecificPantryItem(int pantryId) {

        PantryItem item = null;

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PANTRY,
                null,
                DatabaseHelper.COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(pantryId)},
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {

                item = new PantryItem();

                item.setPantryId(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PANTRY_ID
                                )
                        )
                );

                item.setItemName(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PANTRY_NAME
                                )
                        )
                );

                item.setQuantity(
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PANTRY_QUANTITY
                                )
                        )
                );

                item.setUnit(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PANTRY_UNIT
                                )
                        )
                );

                int expiryIndex = cursor.getColumnIndex(
                        DatabaseHelper.COLUMN_PANTRY_EXPIRY_DATE
                );

                if (expiryIndex >= 0 && !cursor.isNull(expiryIndex)) {
                    item.setExpiryDate(cursor.getString(expiryIndex));
                }
            }

        } finally {
            cursor.close();
        }

        return item;
    }

    // Get all ingredients for a specific recipe.
    public ArrayList<RecipeIngredient> getIngredientsForRecipe(int recipeId) {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_RECIPE_INGREDIENTS,
                null,
                DatabaseHelper.COLUMN_INGREDIENT_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null
        );

        try {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENT_ID);
            int recipeIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENT_RECIPE_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENT_NAME);
            int qtyIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENT_REQUIRED_QTY);
            int unitIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENT_UNIT);

            while (cursor.moveToNext()) {
                RecipeIngredient ing = new RecipeIngredient();
                ing.setIngredientId(cursor.getInt(idIndex));
                ing.setRecipeId(cursor.getInt(recipeIdIndex));
                ing.setIngredientName(cursor.getString(nameIndex));
                ing.setRequiredQuantity(cursor.getDouble(qtyIndex));
                ing.setUnit(cursor.getString(unitIndex));
                ingredients.add(ing);
            }
        } finally {
            cursor.close();
        }
        return ingredients;
    }

    // Get a specific recipe by ID with its ingredients populated.
    public Recipe getSpecificRecipe(int recipeId) {
        Recipe recipe = null;
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_RECIPES,
                null,
                DatabaseHelper.COLUMN_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                recipe = new Recipe();
                recipe.setRecipeId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_ID)));
                recipe.setRecipeName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_NAME)));
                recipe.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_INSTRUCTIONS)));
                recipe.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_CATEGORY)));
                recipe.setIngredients(getIngredientsForRecipe(recipeId));
            }
        } finally {
            cursor.close();
        }
        return recipe;
    }

    // Get all recipes with their ingredients populated.
    public ArrayList<Recipe> getAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_RECIPES,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_RECIPE_NAME + " ASC"
        );

        try {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_NAME);
            int instructionsIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_INSTRUCTIONS);
            int categoryIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_CATEGORY);

            while (cursor.moveToNext()) {
                Recipe recipe = new Recipe();
                int recipeId = cursor.getInt(idIndex);
                recipe.setRecipeId(recipeId);
                recipe.setRecipeName(cursor.getString(nameIndex));
                recipe.setInstructions(cursor.getString(instructionsIndex));
                recipe.setCategory(cursor.getString(categoryIndex));
                recipe.setIngredients(getIngredientsForRecipe(recipeId));
                recipes.add(recipe);
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }
}
