package com.example.foodplanner.presentation.categories.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.presentation.categories.presenter.CategoryMealsPresenterContract;
import com.example.foodplanner.presentation.categories.presenter.CategoryMealsPresenterImpl;
import com.example.foodplanner.data.meal.model.Meal;
import com.example.foodplanner.data.meal.repositry.MealRepository;
import com.example.foodplanner.presentation.search.view.MealAdapter;

import java.util.List;

public class CategoryMealsFragment extends Fragment
        implements CategoryMealsView, MealAdapter.OnMealClickListener {

    private Toolbar toolbar;
    private RecyclerView rvMeals;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private MealAdapter mealAdapter;
    private CategoryMealsPresenterContract presenter;
    private String categoryName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_meals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapter();
        setupPresenter();

        Bundle args = getArguments();
        if (args != null) {
            categoryName = args.getString("categoryName");
            toolbar.setTitle(categoryName);
            presenter.loadMealsByCategory(categoryName);
        }
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvMeals = view.findViewById(R.id.rv_meals);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupAdapter() {
        mealAdapter = new MealAdapter(requireContext(), this);
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupPresenter() {
        presenter = new CategoryMealsPresenterImpl(MealRepository.getInstance(requireContext()));
        presenter.attachView(this);
    }

    // ============ CategoryMealsView Implementation ============

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void showNetworkError() {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), "Network error. Please check your connection.");
        }
    }

    @Override
    public void showMeals(List<Meal> meals) {
        layoutEmpty.setVisibility(View.GONE);
        rvMeals.setVisibility(View.VISIBLE);
        mealAdapter.setMeals(meals);
    }

    @Override
    public void showEmpty() {
        rvMeals.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    // ============ MealAdapter.OnMealClickListener ============

    @Override
    public void onMealClick(String mealId) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_categoryMeals_to_mealDetails, bundle);
    }

    @Override
    public void onFavoriteClick(String mealId) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_categoryMeals_to_mealDetails, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
