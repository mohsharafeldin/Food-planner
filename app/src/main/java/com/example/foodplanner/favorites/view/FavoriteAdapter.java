package com.example.foodplanner.favorites.view;

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
import com.example.foodplanner.data.model.FavoriteMeal;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteMeal> favorites = new ArrayList<>();
    private final Context context;
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteMeal favorite);

        void onRemoveFavorite(FavoriteMeal favorite);
    }

    public FavoriteAdapter(Context context, OnFavoriteClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFavorites(List<FavoriteMeal> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteMeal favorite = favorites.get(position);
        holder.bind(favorite);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivMeal;
        private final TextView tvMealName;
        private final TextView tvMealCategory;
        private final ImageButton btnFavorite;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealCategory = itemView.findViewById(R.id.tv_meal_category);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        void bind(FavoriteMeal favorite) {
            tvMealName.setText(favorite.getStrMeal());
            tvMealCategory.setText(favorite.getStrCategory() + " • " + favorite.getStrArea());
            tvMealCategory.setVisibility(View.VISIBLE);

            // Show filled heart since it's already a favorite
            btnFavorite.setImageResource(R.drawable.ic_favorite);

            Glide.with(context)
                    .load(favorite.getStrMealThumb())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivMeal);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(favorite);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFavorite(favorite);
                }
            });
        }
    }
}
