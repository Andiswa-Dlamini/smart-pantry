package com.example.smart_pantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private ArrayList<Recipe> recipeData;
    private View.OnClickListener mOnItemClickListener;

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        public TextView textRecipeName;
        public TextView textRecipeSubtitle;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textRecipeSubtitle = itemView.findViewById(R.id.textRecipeSubtitle);

            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);
        }

        public TextView getTextRecipeName() {
            return textRecipeName;
        }

        public TextView getTextRecipeSubtitle() {
            return textRecipeSubtitle;
        }
    }

    public RecipeAdapter(ArrayList<Recipe> arrayList, Context context) {
        recipeData = arrayList;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item_row, parent, false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeData.get(position);
        holder.getTextRecipeName().setText(recipe.getRecipeName());

        int ingredientCount = (recipe.getIngredients() != null) ? recipe.getIngredients().size() : 0;
        String subtitle = recipe.getCategory() + " • " + ingredientCount + " ingredients";
        holder.getTextRecipeSubtitle().setText(subtitle);
    }

    @Override
    public int getItemCount() {
        return (recipeData != null) ? recipeData.size() : 0;
    }
}
