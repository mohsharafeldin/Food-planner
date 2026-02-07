package com.example.foodplanner.home.view;

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
import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final Context context;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCategory;
        private final TextView tvCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.iv_category);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }

        void bind(Category category) {
            tvCategoryName.setText(category.getStrCategory());

            Glide.with(context)
                    .load(category.getStrCategoryThumb())
                    .apply(new RequestOptions()
                            .override(300, 300)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivCategory);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
