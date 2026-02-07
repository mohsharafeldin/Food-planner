package com.example.foodplanner.countries.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.search.view.MealAdapter;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CountryMealsFragment extends Fragment implements MealAdapter.OnMealClickListener {

    private Toolbar toolbar;
    private RecyclerView rvMeals;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private MealAdapter mealAdapter;
    private MealRepository repository;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_country_meals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapter();
        loadMeals();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvMeals = view.findViewById(R.id.rv_meals);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        repository = MealRepository.getInstance(requireContext());

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());

        Bundle args = getArguments();
        if (args != null) {
            String countryName = args.getString("countryName");
            toolbar.setTitle(countryName + " Cuisine");
        }
    }

    private void setupAdapter() {
        mealAdapter = new MealAdapter(requireContext(), this);
        rvMeals.setAdapter(mealAdapter);
    }

    private void loadMeals() {
        Bundle args = getArguments();
        if (args == null)
            return;

        String countryName = args.getString("countryName");
        if (countryName == null)
            return;

        progressBar.setVisibility(View.VISIBLE);

        disposables.add(
                repository.filterByArea(countryName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                        layoutEmpty.setVisibility(View.GONE);
                                        rvMeals.setVisibility(View.VISIBLE);
                                        mealAdapter.setMeals(response.getMeals());
                                    } else {
                                        rvMeals.setVisibility(View.GONE);
                                        layoutEmpty.setVisibility(View.VISIBLE);
                                    }
                                },
                                error -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (getContext() != null) {
                                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }));
    }

    @Override
    public void onMealClick(String mealId) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_countryMeals_to_mealDetails, bundle);
    }

    @Override
    public void onFavoriteClick(String mealId) {
        // Navigate to meal details where user can properly add to favorites
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_countryMeals_to_mealDetails, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }
}
