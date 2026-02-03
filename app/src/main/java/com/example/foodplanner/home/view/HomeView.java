package com.example.testfoodplanner.home.view;

import com.example.testfoodplanner.base.BaseView;
import com.example.testfoodplanner.data.model.Area;
import com.example.testfoodplanner.data.model.Category;
import com.example.testfoodplanner.data.model.Meal;

import java.util.List;

public interface HomeView extends BaseView {
    void showMealOfTheDay(Meal meal);

    void showCategories(List<Category> categories);

    void showCountries(List<Area> countries);
}
