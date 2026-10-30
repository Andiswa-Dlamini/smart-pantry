package com.example.smart_pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView rvStrictRecipes;
    private RecyclerView rvAlmostRecipes;
    private TextView textEmptyRecipes;
    private TextView labelAlmostThere;
    private ChipGroup chipGroupCategory;

    private ArrayList<Recipe> allStrictRecipes;
    private ArrayList<Recipe> allAlmostRecipes;
    private ArrayList<Recipe> filteredStrictList;
    private ArrayList<Recipe> filteredAlmostList;

    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;
    private String selectedCategory = "All";

    // Item click listener for strict matches
    private View.OnClickListener onStrictItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && position < filteredStrictList.size()) {
                Recipe selectedRecipe = filteredStrictList.get(position);
                openRecipeDetail(selectedRecipe.getRecipeId());
            }
        }
    };

    // Item click listener for almost matches
    private View.OnClickListener onAlmostItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && position < filteredAlmostList.size()) {
                Recipe selectedRecipe = filteredAlmostList.get(position);
                openRecipeDetail(selectedRecipe.getRecipeId());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Enable Back/Up button in Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Suggested Recipes");
        }

        initViews();
        setupCategoryChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    private void initViews() {
        rvStrictRecipes = findViewById(R.id.rvStrictRecipes);
        rvAlmostRecipes = findViewById(R.id.rvAlmostRecipes);
        textEmptyRecipes = findViewById(R.id.textEmptyRecipes);
        labelAlmostThere = findViewById(R.id.labelAlmostThere);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);

        rvStrictRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvAlmostRecipes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCategoryChips() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    selectedCategory = selectedChip.getText().toString();
                    applyCategoryFilter();
                }
            }
        });
    }

    private void loadSuggestedRecipes() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            ArrayList<PantryItem> pantryItems = ds.getPantryItems();
            ArrayList<Recipe> allRecipes = ds.getAllRecipes();
            ds.close();

            // Use EXISTING RecipeMatcher strict matching logic
            allStrictRecipes = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
            // Use EXISTING RecipeMatcher "Almost There" logic
            allAlmostRecipes = RecipeMatcher.getAlmostMatchingRecipes(allRecipes, pantryItems);

            applyCategoryFilter();

        } catch (Exception e) {
            Toast.makeText(this, "Error matching recipes", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyCategoryFilter() {
        filteredStrictList = new ArrayList<>();
        filteredAlmostList = new ArrayList<>();

        if (allStrictRecipes != null) {
            for (Recipe r : allStrictRecipes) {
                if ("All".equalsIgnoreCase(selectedCategory) ||
                    (r.getCategory() != null && r.getCategory().equalsIgnoreCase(selectedCategory))) {
                    filteredStrictList.add(r);
                }
            }
        }

        if (allAlmostRecipes != null) {
            for (Recipe r : allAlmostRecipes) {
                if ("All".equalsIgnoreCase(selectedCategory) ||
                    (r.getCategory() != null && r.getCategory().equalsIgnoreCase(selectedCategory))) {
                    filteredAlmostList.add(r);
                }
            }
        }

        // Bind Strict Matches
        if (!filteredStrictList.isEmpty()) {
            textEmptyRecipes.setVisibility(View.GONE);
            rvStrictRecipes.setVisibility(View.VISIBLE);

            strictAdapter = new RecipeAdapter(filteredStrictList, this);
            strictAdapter.setOnItemClickListener(onStrictItemClickListener);
            rvStrictRecipes.setAdapter(strictAdapter);
        } else {
            rvStrictRecipes.setVisibility(View.GONE);
            textEmptyRecipes.setVisibility(View.VISIBLE);
        }

        // Bind "Almost There" Matches (missing 1 ingredient)
        if (!filteredAlmostList.isEmpty()) {
            labelAlmostThere.setVisibility(View.VISIBLE);
            rvAlmostRecipes.setVisibility(View.VISIBLE);

            almostAdapter = new RecipeAdapter(filteredAlmostList, this);
            almostAdapter.setOnItemClickListener(onAlmostItemClickListener);
            rvAlmostRecipes.setAdapter(almostAdapter);
        } else {
            labelAlmostThere.setVisibility(View.GONE);
            rvAlmostRecipes.setVisibility(View.GONE);
        }
    }

    private void openRecipeDetail(int recipeId) {
        Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipeID", recipeId);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
