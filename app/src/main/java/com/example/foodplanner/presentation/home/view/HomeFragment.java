package com.example.foodplanner.presentation.home.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodplanner.R;
import com.example.foodplanner.data.model.Area;
import com.example.foodplanner.data.model.Category;
import com.example.foodplanner.data.model.Meal;
import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.presentation.home.presenter.HomePresenterContract;
import com.example.foodplanner.presentation.home.presenter.HomePresenterImpl;
import com.example.foodplanner.utils.NetworkUtils;
import com.example.foodplanner.utils.SessionManager;

import java.util.List;

public class HomeFragment extends Fragment implements HomeView,
        CategoryAdapter.OnCategoryClickListener,
        CountryAdapter.OnCountryClickListener {

    private TextView tvGreeting, tvUserName;
    private CardView cardMealOfDay;
    private ImageView ivMealOfDay;
    private TextView tvMealOfDayName, tvMealOfDayCategory;
    private ProgressBar progressMealOfDay;
    private RecyclerView rvCategories, rvCountries;

    private HomePresenterContract presenter;
    private CategoryAdapter categoryAdapter;
    private CountryAdapter countryAdapter;
    private SessionManager sessionManager;

    private Meal currentMealOfDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupAdapters();
        setupListeners();
        loadData();
    }

    private com.facebook.shimmer.ShimmerFrameLayout shimmerViewContainer;
    private View homeContent;

    // ...

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUserName = view.findViewById(R.id.tv_user_name);
        cardMealOfDay = view.findViewById(R.id.card_meal_of_day);
        ivMealOfDay = view.findViewById(R.id.iv_meal_of_day);
        tvMealOfDayName = view.findViewById(R.id.tv_meal_of_day_name);
        tvMealOfDayCategory = view.findViewById(R.id.tv_meal_of_day_description);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvCountries = view.findViewById(R.id.rv_trending);

        shimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        homeContent = null;

        sessionManager = SessionManager.getInstance();

        // Set user name
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        } else if (sessionManager.isGuest()) {
            tvUserName.setText(R.string.guest_user);
        }

        // Set greeting based on time
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning!");
        } else if (hour < 17) {
            tvGreeting.setText("Good Afternoon!");
        } else {
            tvGreeting.setText("Good Evening!");
        }
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        presenter = new HomePresenterImpl(repository);
        presenter.attachView(this);
    }

    private void setupAdapters() {
        categoryAdapter = new CategoryAdapter(requireContext(), this);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setAdapter(categoryAdapter);
        }

        countryAdapter = new CountryAdapter(requireContext(), this);
        if (rvCountries != null) {
            rvCountries.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            rvCountries.setAdapter(countryAdapter);
        }
    }

    private void setupListeners() {
        cardMealOfDay.setOnClickListener(v -> {
            if (currentMealOfDay != null) {
                navigateToMealDetails(currentMealOfDay.getIdMeal());
            }
        });

        View btnViewRecipe = requireView().findViewById(R.id.btn_view_recipe);
        if (btnViewRecipe != null) {
            btnViewRecipe.setOnClickListener(v -> {
                if (currentMealOfDay != null) {
                    navigateToMealDetails(currentMealOfDay.getIdMeal());
                }
            });
        }
    }

    private void loadData() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            presenter.loadMealOfTheDay();
            presenter.loadCategories();
            presenter.loadCountries();
        } else {
            showNetworkError();
        }
    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        currentMealOfDay = meal;
        tvMealOfDayName.setText(meal.getStrMeal());
        if (tvMealOfDayCategory != null) {
            tvMealOfDayCategory.setText(meal.getStrCategory() + " • " + meal.getStrArea());
        }

        Glide.with(this)
                .load(meal.getStrMealThumb())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_food_logo)
                        .error(R.drawable.ic_food_logo))
                .into(ivMealOfDay);
    }

    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.setCategories(categories);
    }

    @Override
    public void showCountries(List<Area> countries) {
        countryAdapter.setCountries(countries);
        if (rvCountries != null) {
            rvCountries.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showLoading() {
        if (shimmerViewContainer != null) {
            shimmerViewContainer.setVisibility(View.VISIBLE);
            shimmerViewContainer.startShimmer();
        }
        if (homeContent != null) {
            homeContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        if (shimmerViewContainer != null) {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
        }
        if (homeContent != null) {
            homeContent.setVisibility(View.VISIBLE);
        }
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
            SnackbarUtils.showError(getView(), getString(R.string.network_error));
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        Bundle bundle = new Bundle();
        bundle.putString("categoryName", category.getStrCategory());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_categoryMeals, bundle);
    }

    @Override
    public void onCountryClick(Area country) {
        Bundle bundle = new Bundle();
        bundle.putString("countryName", country.getStrArea());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_countryMeals, bundle);
    }

    private void navigateToMealDetails(String mealId) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_mealDetails, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
