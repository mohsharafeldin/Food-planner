package com.example.foodplanner.home.view;

import com.example.foodplanner.base.BaseView;
import com.example.foodplanner.data.model.Area;
import com.example.foodplanner.data.model.Category;
import com.example.foodplanner.data.model.Meal;

import java.util.List;

public interface HomeView extends BaseView {
    void showMealOfTheDay(Meal meal);

    void showCategories(List<Category> categories);

    void showCountries(List<Area> countries);
}
