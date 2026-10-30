package com.example.smart_pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView textRecipeCategory;
    private TextView textRecipeName;
    private TextView textIngredientsList;
    private TextView textInstructions;
    private Button btnShareShoppingList;

    private int recipeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Enable Back/Up button in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }

        initViews();

        // Get the recipe ID passed from RecipeListActivity via Intent
        recipeId = getIntent().getIntExtra("recipeID", -1);
        if (recipeId == -1) {
            recipeId = getIntent().getIntExtra("recipe_id", -1);
        }

        if (recipeId != -1) {
            loadRecipeDetails(recipeId);
        } else {
            Toast.makeText(this, "Error: Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        textRecipeCategory = findViewById(R.id.textRecipeCategory);
        textRecipeName = findViewById(R.id.textRecipeName);
        textIngredientsList = findViewById(R.id.textIngredientsList);
        textInstructions = findViewById(R.id.textInstructions);
        btnShareShoppingList = findViewById(R.id.btnShareShoppingList);
    }

    private void loadRecipeDetails(int id) {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            Recipe recipe = ds.getSpecificRecipe(id);
            ArrayList<PantryItem> pantryItems = ds.getPantryItems();
            ds.close();

            if (recipe != null) {
                textRecipeCategory.setText(recipe.getCategory());
                textRecipeName.setText(recipe.getRecipeName());
                textInstructions.setText(recipe.getInstructions());

                // Format ingredient list
                ArrayList<RecipeIngredient> ingredients = recipe.getIngredients();
                if (ingredients != null && !ingredients.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (RecipeIngredient ing : ingredients) {
                        sb.append("• ")
                          .append(ing.getIngredientName())
                          .append(" - ")
                          .append(ing.getRequiredQuantity())
                          .append(" ")
                          .append(ing.getUnit())
                          .append("\n");
                    }
                    textIngredientsList.setText(sb.toString().trim());
                } else {
                    textIngredientsList.setText("No ingredients listed.");
                }

                // Compute missing ingredients for shopping list
                ArrayList<RecipeIngredient> missing = RecipeMatcher.getMissingIngredientsForRecipe(recipe, pantryItems);
                setupShoppingListButton(recipe, missing);
            } else {
                Toast.makeText(this, "Failed to load recipe", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error accessing database", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupShoppingListButton(Recipe recipe, ArrayList<RecipeIngredient> missing) {
        if (missing == null || missing.isEmpty()) {
            btnShareShoppingList.setText("All Ingredients Available in Pantry!");
        } else {
            btnShareShoppingList.setText("Export Shopping List (" + missing.size() + " Missing)");
        }

        btnShareShoppingList.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Smart Pantry - Shopping List\n");
            sb.append("Recipe: ").append(recipe.getRecipeName()).append("\n\n");

            if (missing != null && !missing.isEmpty()) {
                sb.append("Missing / Needed Ingredients:\n");
                for (RecipeIngredient ing : missing) {
                    sb.append("• ")
                      .append(ing.getIngredientName())
                      .append(" - ")
                      .append(ing.getRequiredQuantity())
                      .append(" ")
                      .append(ing.getUnit())
                      .append("\n");
                }
            } else {
                sb.append("Required Ingredients:\n");
                for (RecipeIngredient ing : recipe.getIngredients()) {
                    sb.append("• ")
                      .append(ing.getIngredientName())
                      .append(" - ")
                      .append(ing.getRequiredQuantity())
                      .append(" ")
                      .append(ing.getUnit())
                      .append("\n");
                }
            }

            // Launch native Android text sharing Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shopping List for " + recipe.getRecipeName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString().trim());

            startActivity(Intent.createChooser(shareIntent, "Export Shopping List via"));
        });
    }

    // Handle Back/Up button click in Action Bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
