package com.example.foodplanner.planner.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodplanner.R;

public class PlannerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSection(view.findViewById(R.id.section_breakfast), "Breakfast");
        setupSection(view.findViewById(R.id.section_lunch), "Lunch");
        setupSection(view.findViewById(R.id.section_dessert), "Dessert");
    }

    private void setupSection(View sectionView, String title) {
        if (sectionView == null)
            return;

        android.widget.TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        android.widget.TextView tvAddLabel = sectionView.findViewById(R.id.tv_add_meal_label);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }

        if (tvAddLabel != null) {
            tvAddLabel.setText("Add " + title);
        }
    }
}
