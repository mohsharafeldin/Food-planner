package com.example.foodplanner.planner.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.planner.presenter.PlannerPresenter;
import com.example.foodplanner.utils.Constants;
import com.example.foodplanner.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlannerFragment extends Fragment implements PlannerView {

    private PlannerPresenter presenter;
    private SessionManager sessionManager;
    private String currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Section views
    private View sectionBreakfast, sectionLunch, sectionDinner, sectionSnacks;
    private LinearLayout calendarStrip;
    private TextView tvMonthYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (sessionManager.isGuest()) {
            // return; // Don't return, let UI initialize under overlay
        }

        initPresenter();
        setupCalendarStrip();
        setupSections();

        // Default to today's date
        currentDate = dateFormat.format(new Date());
        loadMealsForDate(currentDate);
    }

    private String currentUserId;

    @Override
    public void onResume() {
        super.onResume();
        // Clear any pending plan selection when returning to planner
        if (sessionManager != null) {
            sessionManager.clearPlanSelection();
        } else {
            sessionManager = new SessionManager(requireContext());
            currentUserId = sessionManager.getUserId();
        }

        if (sessionManager.isGuest()) {
            return;
        }

        // Check if user has changed
        String newUserId = sessionManager.getUserId();
        if (currentUserId == null || !newUserId.equals(currentUserId)) {
            if (presenter != null) {
                presenter.detachView();
            }
            currentUserId = newUserId;
            initPresenter();
            // Reload for current date with new user
            loadMealsForDate(currentDate);
        } else {
            // User is same, maybe just reload to be safe?
            // loadMealsForDate(currentDate);
            // Actually, if we came back from adding a meal, we might want to reload.
            // But let's stick to the user change logic for now.
        }
    }

    private Calendar currentWeekStart;
    private ImageView btnPrevWeek, btnNextWeek;

    private void initViews(View view) {
        sectionBreakfast = view.findViewById(R.id.section_breakfast);
        sectionLunch = view.findViewById(R.id.section_lunch);
        sectionDinner = view.findViewById(R.id.section_dinner);
        sectionSnacks = view.findViewById(R.id.section_snacks);
        calendarStrip = view.findViewById(R.id.layout_calendar_strip);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);

        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getUserId();

        // Guest Mode Logic
        if (sessionManager.isGuest()) {
            View guestLayout = view.findViewById(R.id.layout_guest_mode);
            if (guestLayout != null) {
                guestLayout.setVisibility(View.VISIBLE);
                guestLayout.findViewById(R.id.btn_guest_login).setOnClickListener(v -> {
                    sessionManager.logout();
                    Navigation.findNavController(view).navigate(R.id.action_planner_to_auth);
                });
            }
            // Hide main content
            View content = (View) view.findViewById(R.id.layout_calendar_strip).getParent().getParent(); // Get
                                                                                                         // NestedScrollView
            if (content instanceof androidx.core.widget.NestedScrollView) {
                ((View) content).setVisibility(View.GONE);
            }
            // Also hide sections just in case
            ((View) sectionBreakfast.getParent()).setVisibility(View.GONE);

            // Also hide AppBarLayout
            View calendarStripParent = (View) calendarStrip.getParent(); // HorizontalScrollView
            View headerLinear = (View) calendarStripParent.getParent(); // LinearLayout
            View appBarLayout = (View) headerLinear.getParent(); // AppBarLayout

            if (appBarLayout instanceof com.google.android.material.appbar.AppBarLayout) {
                appBarLayout.setVisibility(View.GONE);
            }
        }

        currentWeekStart = Calendar.getInstance();

        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.DAY_OF_YEAR, -7);
                setupCalendarStrip();
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.DAY_OF_YEAR, 7);
                setupCalendarStrip();
            });
        }
    }

    // ... existing code ...

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        // Use currentUserId which is updated
        presenter = new PlannerPresenter(repository, currentUserId);
        presenter.attachView(this);
    }

    private void setupCalendarStrip() {
        if (calendarStrip == null)
            return;

        calendarStrip.removeAllViews();
        Calendar iterator = (Calendar) currentWeekStart.clone();

        // Set month/year header based on the first day of the week
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        if (tvMonthYear != null) {
            tvMonthYear.setText(monthFormat.format(iterator.getTime()));
        }

        // Show 7 days starting from currentWeekStart
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            View dayView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_calendar_day, calendarStrip, false);

            TextView tvDayName = dayView.findViewById(R.id.tv_day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);
            CardView cardDay = dayView.findViewById(R.id.card_day);

            Date date = iterator.getTime();
            String dateString = dateFormat.format(date);

            if (tvDayName != null)
                tvDayName.setText(dayNameFormat.format(date));
            if (tvDayNumber != null)
                tvDayNumber.setText(dayNumFormat.format(date));

            // Highlight if this day matches the currently selected date
            boolean isSelected = dateString.equals(currentDate);

            if (isSelected && cardDay != null) {
                cardDay.setCardBackgroundColor(getResources().getColor(R.color.primary_color, null));
                if (tvDayName != null)
                    tvDayName.setTextColor(getResources().getColor(R.color.background_dark, null));
                if (tvDayNumber != null)
                    tvDayNumber.setTextColor(getResources().getColor(R.color.background_dark, null));
            } else {
                // Reset if not selected (crucial for recycled views or re-layout)
                if (cardDay != null)
                    cardDay.setCardBackgroundColor(getResources().getColor(R.color.surface_dark, null));
                if (tvDayName != null)
                    tvDayName.setTextColor(getResources().getColor(R.color.text_secondary, null));
                if (tvDayNumber != null)
                    tvDayNumber.setTextColor(getResources().getColor(R.color.text_primary, null));
            }

            dayView.setOnClickListener(v -> {
                if (sessionManager.isGuest())
                    return; // Block interaction
                currentDate = dateString;
                loadMealsForDate(currentDate);
                // Refresh strip to update selection highlight
                setupCalendarStrip();
            });

            calendarStrip.addView(dayView);
            iterator.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void highlightSelectedDay(int selectedIndex) {
        for (int i = 0; i < calendarStrip.getChildCount(); i++) {
            View dayView = calendarStrip.getChildAt(i);
            CardView cardDay = dayView.findViewById(R.id.card_day);
            TextView tvDayName = dayView.findViewById(R.id.tv_day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);

            if (i == selectedIndex) {
                if (cardDay != null)
                    cardDay.setCardBackgroundColor(getResources().getColor(R.color.primary_color, null));
                if (tvDayName != null)
                    tvDayName.setTextColor(getResources().getColor(R.color.background_dark, null));
                if (tvDayNumber != null)
                    tvDayNumber.setTextColor(getResources().getColor(R.color.background_dark, null));
            } else {
                if (cardDay != null)
                    cardDay.setCardBackgroundColor(getResources().getColor(R.color.surface_dark, null));
                if (tvDayName != null)
                    tvDayName.setTextColor(getResources().getColor(R.color.text_secondary, null));
                if (tvDayNumber != null)
                    tvDayNumber.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
        }
    }

    private void setupSections() {
        setupSection(sectionBreakfast, "Breakfast", Constants.MEAL_TYPE_BREAKFAST);
        setupSection(sectionLunch, "Lunch", Constants.MEAL_TYPE_LUNCH);
        setupSection(sectionDinner, "Dinner", Constants.MEAL_TYPE_DINNER);
        setupSection(sectionSnacks, "Dessert", Constants.MEAL_TYPE_SNACKS);
    }

    private void setupSection(View sectionView, String title, String mealType) {
        if (sectionView == null)
            return;

        TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        TextView tvAddLabel = sectionView.findViewById(R.id.tv_add_meal_label);
        CardView cardAddMeal = sectionView.findViewById(R.id.card_add_meal);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }

        if (tvAddLabel != null) {
            tvAddLabel.setText("Add " + title);
        }

        // Setup click handler for add meal card
        if (cardAddMeal != null) {
            cardAddMeal.setOnClickListener(v -> {
                if (sessionManager.isGuest()) {
                    Toast.makeText(requireContext(), "Please login to add meals to plan", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the selected date and meal type
                sessionManager.savePlanSelection(currentDate, mealType);

                // Navigate to search to pick a meal
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_planner_to_search);
            });
        }
    }

    private void loadMealsForDate(String date) {
        if (sessionManager.isGuest()) {
            return;
        }
        presenter.loadPlannedMeals(date);
    }

    @Override
    public void showPlannedMeals(List<PlannedMeal> meals) {
        // Reset all sections to empty state first
        resetSection(sectionBreakfast);
        resetSection(sectionLunch);
        resetSection(sectionDinner);
        resetSection(sectionSnacks);

        // Populate sections with meals
        for (PlannedMeal meal : meals) {
            View section = getSectionForMealType(meal.getMealType());
            if (section != null) {
                showMealInSection(section, meal);
            }
        }
    }

    private View getSectionForMealType(String mealType) {
        if (mealType == null)
            return null;
        switch (mealType) {
            case Constants.MEAL_TYPE_BREAKFAST:
                return sectionBreakfast;
            case Constants.MEAL_TYPE_LUNCH:
                return sectionLunch;
            case Constants.MEAL_TYPE_DINNER:
                return sectionDinner;
            case Constants.MEAL_TYPE_SNACKS:
                return sectionSnacks;
            default:
                return null;
        }
    }

    private void resetSection(View section) {
        if (section == null)
            return;

        CardView cardMeal = section.findViewById(R.id.card_meal);
        CardView cardAddMeal = section.findViewById(R.id.card_add_meal);

        if (cardMeal != null)
            cardMeal.setVisibility(View.GONE);
        if (cardAddMeal != null)
            cardAddMeal.setVisibility(View.VISIBLE);
    }

    private void showMealInSection(View section, PlannedMeal meal) {
        if (section == null || meal == null)
            return;

        CardView cardMeal = section.findViewById(R.id.card_meal);
        CardView cardAddMeal = section.findViewById(R.id.card_add_meal);
        TextView tvMealName = section.findViewById(R.id.tv_meal_name);
        ImageView ivMealImage = section.findViewById(R.id.iv_meal_image);
        View btnDelete = section.findViewById(R.id.btn_meal_options);

        if (cardAddMeal != null)
            cardAddMeal.setVisibility(View.GONE);
        if (cardMeal != null)
            cardMeal.setVisibility(View.VISIBLE);

        if (tvMealName != null) {
            tvMealName.setText(meal.getMealName());
        }

        if (ivMealImage != null && meal.getMealThumb() != null) {
            Glide.with(this)
                    .load(meal.getMealThumb())
                    .placeholder(R.drawable.ic_food_logo)
                    .into(ivMealImage);
        }

        // Click to view details
        if (cardMeal != null) {
            cardMeal.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("mealId", meal.getMealId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_planner_to_mealDetails, args);
            });
        }

        // Delete button
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                presenter.removePlannedMeal(meal);
            });
        }
    }

    @Override
    public void showEmptyPlanner() {
        resetSection(sectionBreakfast);
        resetSection(sectionLunch);
        resetSection(sectionDinner);
        resetSection(sectionSnacks);
    }

    @Override
    public void onMealRemoved() {
        Toast.makeText(requireContext(), R.string.meal_removed_from_plan, Toast.LENGTH_SHORT).show();
        // Reload meals for current date
        loadMealsForDate(currentDate);
    }

    @Override
    public void showLoading() {
        // Could show shimmer or progress bar
    }

    @Override
    public void hideLoading() {
        // Hide shimmer or progress bar
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
