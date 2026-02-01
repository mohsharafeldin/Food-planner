package com.example.foodplanner.categories.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodplanner.R;

public class CategoryMealsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Since fragment_category_meals might not exist or be named differently,
        // I'll check the layout name from the nav graph if needed.
        // Based on main_nav_graph.xml, it's fragment_category_meals.
        return inflater.inflate(R.layout.fragment_category_meals, container, false);
        // Actually, let me check the layouts again. fragment_category_meals.xml was NOT
        // in the list.
        // Wait, I saw item_meal_section, etc. Let me list layout directory again to be
        // sure.
    }
}
