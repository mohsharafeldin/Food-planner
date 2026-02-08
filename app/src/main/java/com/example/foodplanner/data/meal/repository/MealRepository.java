package com.example.foodplanner.data.meal.repository;

import android.content.Context;

import com.example.foodplanner.data.meal.local.AppDatabase;
import com.example.foodplanner.data.meal.local.FavoriteMealDAO;
import com.example.foodplanner.data.meal.local.PlannedMealDAO;
import com.example.foodplanner.data.meal.model.AreaResponse;
import com.example.foodplanner.data.meal.model.CategoryResponse;
import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.IngredientResponse;
import com.example.foodplanner.data.meal.model.Meal;
import com.example.foodplanner.data.meal.model.MealResponse;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.example.foodplanner.data.meal.remote.MealApiService;
import com.example.foodplanner.data.meal.remote.RetrofitClient;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MealRepository {

    private static volatile MealRepository instance;
    private final MealApiService apiService;
    private final FavoriteMealDAO favoriteMealDAO;
    private final PlannedMealDAO plannedMealDAO;

    private MealRepository(Context context) {
        apiService = RetrofitClient.getInstance().getMealApiService();
        AppDatabase database = AppDatabase.getInstance(context);
        favoriteMealDAO = database.favoriteMealDAO();
        plannedMealDAO = database.plannedMealDAO();
    }

    public static synchronized MealRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MealRepository(context);
        }
        return instance;
    }

    // ============ Remote API Methods ============

    public Single<MealResponse> getRandomMeal() {
        return apiService.getRandomMeal();
    }

    public Single<MealResponse> searchMealByName(String name) {
        return apiService.searchMealByName(name);
    }

    public Single<MealResponse> getMealById(String id) {
        return apiService.getMealById(id);
    }

    public Single<CategoryResponse> getAllCategories() {
        return apiService.getAllCategories();
    }

    public Single<AreaResponse> getAllAreas() {
        return apiService.getAllAreas();
    }

    public Single<IngredientResponse> getAllIngredients() {
        return apiService.getAllIngredients();
    }

    public Single<MealResponse> filterByCategory(String category) {
        return apiService.filterByCategory(category);
    }

    public Single<MealResponse> filterByArea(String area) {
        return apiService.filterByArea(area);
    }

    public Single<MealResponse> filterByIngredient(String ingredient) {
        return apiService.filterByIngredient(ingredient);
    }

    // ============ Favorite Meals Methods ============

    public Completable addFavorite(Meal meal, String userId) {
        FavoriteMeal favMeal = FavoriteMeal.fromMeal(meal, userId);
        return favoriteMealDAO.insertFavorite(favMeal);
    }

    public Completable removeFavorite(FavoriteMeal meal) {
        return favoriteMealDAO.deleteFavorite(meal);
    }

    public Completable removeFavoriteById(String mealId, String userId) {
        return favoriteMealDAO.deleteFavoriteById(mealId, userId);
    }

    public Flowable<List<FavoriteMeal>> getAllFavorites(String userId) {
        return favoriteMealDAO.getAllFavorites(userId);
    }

    public Single<Boolean> isFavorite(String mealId, String userId) {
        return favoriteMealDAO.isFavorite(mealId, userId);
    }

    // ============ Planned Meals Methods ============

    public Completable addPlannedMeal(Meal meal, String date, String mealType, String userId) {
        PlannedMeal plannedMeal = PlannedMeal.fromMeal(meal, date, mealType, userId);
        return plannedMealDAO.insertPlannedMeal(plannedMeal);
    }

    public Completable removePlannedMeal(PlannedMeal meal) {
        return plannedMealDAO.deletePlannedMeal(meal);
    }

    public Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId) {
        return plannedMealDAO.getAllPlannedMeals(userId);
    }

    public Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId) {
        return plannedMealDAO.getPlannedMealsByDate(date, userId);
    }

    public Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId) {
        return plannedMealDAO.getPlannedMealsForWeek(startDate, endDate, userId);
    }

    public Flowable<List<PlannedMeal>> getPlannedMealsByDateRange(String startDate, String endDate, String userId) {
        return plannedMealDAO.getPlannedMealsForWeek(startDate, endDate, userId);
    }

    public Single<MealResponse> searchMealsByName(String name) {
        return apiService.searchMealByName(name);
    }

    // ============ Backup/Sync Methods ============

    public Completable deleteAllFavorites(String userId) {
        return favoriteMealDAO.deleteAllFavorites(userId);
    }

    public Completable insertAllFavorites(List<FavoriteMeal> meals) {
        return favoriteMealDAO.insertAllFavorites(meals);
    }

    public Completable deleteAllPlannedMeals(String userId) {
        return plannedMealDAO.deleteAllPlannedMeals(userId);
    }

    public Completable insertAllPlannedMeals(List<PlannedMeal> meals) {
        return plannedMealDAO.insertAllPlannedMeals(meals);
    }

    public List<PlannedMeal> getAllPlannedMealsSync(String userId) {
        return plannedMealDAO.getAllPlannedMealsSync(userId);
    }

    public Completable insertPlannedMealDirectly(PlannedMeal meal) {
        return plannedMealDAO.insertPlannedMeal(meal);
    }

    public Completable addFavoriteDirectly(FavoriteMeal meal) {
        if (meal.getUserId() == null || meal.getUserId().isEmpty()) {
            return Completable.error(new IllegalArgumentException("User ID is required for favorites"));
        }
        return favoriteMealDAO.insertFavorite(meal);
    }
}
