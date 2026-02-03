package com.example.testfoodplanner.mealdetails.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.testfoodplanner.R;
import com.example.testfoodplanner.data.model.IngredientWithMeasure;

import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<IngredientWithMeasure> ingredients = new ArrayList<>();
    private final Context context;

    public IngredientAdapter(Context context) {
        this.context = context;
    }

    public void setIngredients(List<IngredientWithMeasure> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    public String getIngredientsText() {
        StringBuilder builder = new StringBuilder();
        for (IngredientWithMeasure item : ingredients) {
            builder.append(item.getIngredient())
                    .append(" (")
                    .append(item.getMeasure())
                    .append("), ");
        }
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }
        return builder.toString();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientWithMeasure ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIngredient;
        private final TextView tvIngredientName;
        private final TextView tvIngredientMeasure;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIngredient = itemView.findViewById(R.id.iv_ingredient);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
            tvIngredientMeasure = itemView.findViewById(R.id.tv_ingredient_measure);
        }

        void bind(IngredientWithMeasure ingredient) {
            tvIngredientName.setText(ingredient.getIngredient());
            tvIngredientMeasure.setText(ingredient.getMeasure());

            Glide.with(context)
                    .load(ingredient.getIngredientImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivIngredient);
        }
    }
}
