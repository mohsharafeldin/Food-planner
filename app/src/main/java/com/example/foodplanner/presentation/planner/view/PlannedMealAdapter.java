package com.example.foodplanner.presentation.planner.view;

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
import com.example.foodplanner.data.meal.model.PlannedMeal;

import java.util.ArrayList;
import java.util.List;

public class PlannedMealAdapter extends RecyclerView.Adapter<PlannedMealAdapter.PlannedMealViewHolder> {

    private List<PlannedMeal> plannedMeals = new ArrayList<>();
    private final Context context;
    private final OnPlannedMealClickListener listener;

    public interface OnPlannedMealClickListener {
        void onPlannedMealClick(PlannedMeal meal);

        void onDeletePlannedMeal(PlannedMeal meal);
    }

    public PlannedMealAdapter(Context context, OnPlannedMealClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setPlannedMeals(List<PlannedMeal> plannedMeals) {
        this.plannedMeals = plannedMeals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlannedMealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_planned_meal, parent, false);
        return new PlannedMealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlannedMealViewHolder holder, int position) {
        PlannedMeal meal = plannedMeals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return plannedMeals.size();
    }

    class PlannedMealViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMealType;
        private final ImageButton btnDelete;
        private final ImageView ivMeal;
        private final TextView tvMealName;
        private final TextView tvMealCategory;

        PlannedMealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealCategory = itemView.findViewById(R.id.tv_meal_category);
        }

        void bind(PlannedMeal meal) {
            tvMealType.setText(meal.getMealType());
            tvMealName.setText(meal.getMealName());
            tvMealCategory.setText(meal.getMealCategory());

            Glide.with(context)
                    .load(meal.getMealThumb())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivMeal);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlannedMealClick(meal);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePlannedMeal(meal);
                }
            });
        }
    }
}
