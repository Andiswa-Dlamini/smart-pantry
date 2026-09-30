package com.example.smart_pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PantryActivity extends AppCompatActivity {

    private ArrayList<PantryItem> pantryList;
    private PantryAdapter pantryAdapter;
    private RecyclerView recyclerViewPantry;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            int pantryId = pantryList.get(position).getPantryId();

            // Pass selected pantry item ID to Add/Edit activity via Intent Extra
            Intent intent = new Intent(PantryActivity.this, AddEditPantryActivity.class);
            intent.putExtra("pantryID", pantryId);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        initAddIngredientButton();
        initDeleteSwitch();
        initNavigationButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            pantryList = ds.getPantryItems();
            ds.close();

            recyclerViewPantry = findViewById(R.id.rvPantry);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerViewPantry.setLayoutManager(layoutManager);

            pantryAdapter = new PantryAdapter(pantryList, this);
            pantryAdapter.setOnItemClickListener(onItemClickListener);
            recyclerViewPantry.setAdapter(pantryAdapter);

        } catch (Exception e) {
            Toast.makeText(this, "Error retrieving pantry items", Toast.LENGTH_LONG).show();
        }
    }

    private void initAddIngredientButton() {
        Button newIngredient = findViewById(R.id.buttonAddIngredient);
        newIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PantryActivity.this, AddEditPantryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDeleteSwitch() {
        Switch s = findViewById(R.id.switchDelete);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (pantryAdapter != null) {
                    pantryAdapter.setDelete(isChecked);
                    pantryAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initNavigationButtons() {
        Button buttonViewRecipes = findViewById(R.id.buttonViewRecipes);
        buttonViewRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PantryActivity.this, RecipeListActivity.class);
                startActivity(intent);
            }
        });

        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PantryActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
