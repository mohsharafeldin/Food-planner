package com.example.foodplanner.search.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodplanner.R;
import com.example.foodplanner.data.model.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> meals = new ArrayList<>();
    private final Context context;
    private final OnMealClickListener listener;

    public interface OnMealClickListener {
        void onMealClick(String mealId);

        void onFavoriteClick(String mealId);
    }

    public MealAdapter(Context context, OnMealClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals != null ? meals : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivMeal;
        private final TextView tvMealName;
        private final TextView tvMealCategory;
        private final ImageButton btnFavorite;

        MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealCategory = itemView.findViewById(R.id.tv_meal_category);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        void bind(Meal meal) {
            tvMealName.setText(meal.getStrMeal());

            if (meal.getStrCategory() != null && meal.getStrArea() != null) {
                tvMealCategory.setText(meal.getStrCategory() + " • " + meal.getStrArea());
                tvMealCategory.setVisibility(View.VISIBLE);
            } else if (meal.getStrCategory() != null) {
                tvMealCategory.setText(meal.getStrCategory());
                tvMealCategory.setVisibility(View.VISIBLE);
            } else {
                tvMealCategory.setVisibility(View.GONE);
            }

            Glide.with(context)
                    .load(meal.getStrMealThumb())
                    .apply(new RequestOptions()
                            .override(500, 500)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivMeal);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(meal.getIdMeal());
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(meal.getIdMeal());
                }
            });
        }
    }
}
