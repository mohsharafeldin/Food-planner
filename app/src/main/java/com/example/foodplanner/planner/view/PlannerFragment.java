package com.example.foodplanner.planner.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.foodplanner.R;
import com.example.foodplanner.data.model.PlannedMeal;
import com.example.foodplanner.data.repository.MealRepository;
import com.example.foodplanner.planner.presenter.PlannerPresenter;
import com.example.foodplanner.utils.Constants;
import com.example.foodplanner.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlannerFragment extends Fragment implements PlannerView {

    private TextView tvMonthYear;
    private View sectionBreakfast, sectionLunch, sectionDinner, sectionSnacks;
    private LinearLayout layoutCalendarStrip;

    private PlannerPresenter presenter;
    private SessionManager sessionManager;

    private Calendar currentWeekStart = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

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
        initPresenter();
        setupCalendar();
        setupListeners();

        // Initial load
        loadMealsForDate(selectedDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Clear any pending plan selection when user returns to Planner (cancels the
        // flow)
        if (sessionManager != null) {
            sessionManager.clearPlanSelection();
        }
        // Reload meals for the selected date
        loadMealsForDate(selectedDate);
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        layoutCalendarStrip = view.findViewById(R.id.layout_calendar_strip);

        sectionBreakfast = view.findViewById(R.id.section_breakfast);
        sectionLunch = view.findViewById(R.id.section_lunch);
        sectionDinner = view.findViewById(R.id.section_dinner);
        sectionSnacks = view.findViewById(R.id.section_snacks);

        setupSectionTitle(sectionBreakfast, "Breakfast", R.drawable.ic_time);
        setupSectionTitle(sectionLunch, "Lunch", R.drawable.ic_time);
        setupSectionTitle(sectionDinner, "Dinner", R.drawable.ic_time);
        setupSectionTitle(sectionSnacks, "Snack", R.drawable.ic_time);

        sessionManager = new SessionManager(requireContext());
    }

    private void setupSectionTitle(View section, String title, int iconRes) {
        if (section == null)
            return;
        TextView tvTitle = section.findViewById(R.id.tv_section_title);
        TextView tvAddLabel = section.findViewById(R.id.tv_add_meal_label);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (tvAddLabel != null) {
            tvAddLabel.setText("Add " + title);
        }
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        String userId = sessionManager.getUserId();
        presenter = new PlannerPresenter(repository, userId);
        presenter.attachView(this);
    }

    private void setupCalendar() {
        if (layoutCalendarStrip == null)
            return;
        layoutCalendarStrip.removeAllViews();

        tvMonthYear.setText(monthYearFormat.format(currentWeekStart.getTime()));

        // Display 7 days starting from currentWeekStart (which starts at today)
        Calendar cal = (Calendar) currentWeekStart.clone();
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            View dayView = LayoutInflater.from(requireContext()).inflate(R.layout.item_calendar_day,
                    layoutCalendarStrip, false);
            TextView tvDayName = dayView.findViewById(R.id.tv_day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);

            // Show actual day name (Mon, Tue, etc.) based on the date
            tvDayName.setText(dayNameFormat.format(cal.getTime()));
            tvDayNumber.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            final Calendar dayDate = (Calendar) cal.clone();

            // Highlight selected day
            if (isSameDay(cal, selectedDate)) {
                dayView.setBackgroundResource(R.drawable.bg_calendar_selected);
                tvDayName.setTextColor(getResources().getColor(R.color.background_dark, null));
                tvDayNumber.setTextColor(getResources().getColor(R.color.background_dark, null));
            } else {
                dayView.setBackgroundResource(R.drawable.bg_calendar_default);
                tvDayName.setTextColor(getResources().getColor(R.color.text_secondary, null));
                tvDayNumber.setTextColor(getResources().getColor(R.color.text_primary, null));
            }

            dayView.setOnClickListener(v -> {
                selectedDate = dayDate;
                setupCalendar(); // Refresh UI
                loadMealsForDate(selectedDate);
            });

            layoutCalendarStrip.addView(dayView);

            // Add spacing
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dayView.getLayoutParams();
            params.setMarginEnd(16);
            dayView.setLayoutParams(params);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void setupListeners() {
        FloatingActionButton fab = requireView().findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.searchFragment));

        // Previous week button (using menu button)
        ImageButton btnPrevWeek = requireView().findViewById(R.id.btn_menu);
        if (btnPrevWeek != null) {
            btnPrevWeek.setImageResource(R.drawable.ic_arrow_back);
            btnPrevWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
                selectedDate = (Calendar) currentWeekStart.clone();
                setupCalendar();
                loadMealsForDate(selectedDate);
            });
        }

        // Next week button (using calendar button)
        ImageButton btnNextWeek = requireView().findViewById(R.id.btn_calendar);
        if (btnNextWeek != null) {
            btnNextWeek.setImageResource(R.drawable.ic_arrow_forward);
            btnNextWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.DAY_OF_MONTH, 7);
                selectedDate = (Calendar) currentWeekStart.clone();
                setupCalendar();
                loadMealsForDate(selectedDate);
            });
        }

        // Add listeners for "Add Meal" cards in sections
        setupAddButton(sectionBreakfast, Constants.MEAL_TYPE_BREAKFAST);
        setupAddButton(sectionLunch, Constants.MEAL_TYPE_LUNCH);
        setupAddButton(sectionDinner, Constants.MEAL_TYPE_DINNER);
        setupAddButton(sectionSnacks, "Snack");
    }

    private void setupAddButton(View section, String mealType) {
        if (section == null)
            return;
        View cardAdd = section.findViewById(R.id.card_add_meal);
        cardAdd.setOnClickListener(v -> {
            // Save selected date and meal type for the meal planning flow
            String dateStr = dateFormat.format(selectedDate.getTime());
            sessionManager.setSelectedPlanDate(dateStr);
            sessionManager.setSelectedMealType(mealType);
            // Navigate to Search tab via BottomNavigationView to maintain proper back stack
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.searchFragment);
            }
        });
    }

    private void loadMealsForDate(Calendar date) {
        String dateStr = dateFormat.format(date.getTime());
        presenter.loadPlannedMeals(dateStr);
    }

    @Override
    public void showPlannedMeals(List<PlannedMeal> meals) {
        // Clear all sections first (show Empty state)
        clearSection(sectionBreakfast);
        clearSection(sectionLunch);
        clearSection(sectionDinner);
        clearSection(sectionSnacks);

        for (PlannedMeal meal : meals) {
            if (Constants.MEAL_TYPE_BREAKFAST.equalsIgnoreCase(meal.getMealType())) {
                populateSection(sectionBreakfast, meal);
            } else if (Constants.MEAL_TYPE_LUNCH.equalsIgnoreCase(meal.getMealType())) {
                populateSection(sectionLunch, meal);
            } else if (Constants.MEAL_TYPE_DINNER.equalsIgnoreCase(meal.getMealType())) {
                populateSection(sectionDinner, meal);
            } else if ("Snack".equalsIgnoreCase(meal.getMealType())) {
                populateSection(sectionSnacks, meal);
            }
        }
    }

    private void clearSection(View section) {
        if (section == null)
            return;
        section.findViewById(R.id.card_meal).setVisibility(View.GONE);
        section.findViewById(R.id.card_add_meal).setVisibility(View.VISIBLE);
    }

    private void populateSection(View section, PlannedMeal meal) {
        if (section == null)
            return;

        View cardMeal = section.findViewById(R.id.card_meal);
        View cardAdd = section.findViewById(R.id.card_add_meal);

        cardAdd.setVisibility(View.GONE);
        cardMeal.setVisibility(View.VISIBLE);

        TextView tvName = section.findViewById(R.id.tv_meal_name);
        TextView tvCalories = section.findViewById(R.id.tv_meal_calories);
        android.widget.ImageView ivImage = section.findViewById(R.id.iv_meal_image);
        View btnDelete = section.findViewById(R.id.btn_meal_options);

        tvName.setText(meal.getMealName());
        // tvCalories.setText(...) // If we had calories

        Glide.with(this)
                .load(meal.getMealThumb())
                .into(ivImage);

        btnDelete.setOnClickListener(v -> {
            presenter.removePlannedMeal(meal);
        });

        cardMeal.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("mealId", meal.getMealId());
            Navigation.findNavController(requireView()).navigate(R.id.action_planner_to_mealDetails, bundle);
        });
    }

    @Override
    public void showEmptyPlanner() {
        showPlannedMeals(new ArrayList<>());
    }

    @Override
    public void onMealRemoved() {
        Toast.makeText(requireContext(), R.string.meal_removed_from_plan, Toast.LENGTH_SHORT).show();
        loadMealsForDate(selectedDate);
    }

    @Override
    public void showLoading() {
        // Optional: show progress
    }

    @Override
    public void hideLoading() {
        // Optional: hide progress
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
        presenter.detachView();
    }
}
