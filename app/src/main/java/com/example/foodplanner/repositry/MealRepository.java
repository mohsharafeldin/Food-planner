package com.example.foodplanner.repositry;

import android.content.Context;

import com.example.foodplanner.data.firebase.FirebaseSyncHelper;
import com.example.foodplanner.data.db.AppDatabase;
import com.example.foodplanner.data.datasource.local.MealLocalDataSource;
import com.example.foodplanner.data.datasource.local.MealLocalDataSourceImpl;
import com.example.foodplanner.data.datasource.remote.MealRemoteDataSource;
import com.example.foodplanner.data.datasource.remote.MealRemoteDataSourceImpl;
import com.example.foodplanner.data.model.AreaResponse;
import com.example.foodplanner.data.model.CategoryResponse;
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.data.model.IngredientResponse;
import com.example.foodplanner.data.model.Meal;
import com.example.foodplanner.data.model.MealResponse;
import com.example.foodplanner.data.model.PlannedMeal;
import com.example.foodplanner.data.network.Network;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository implementation that coordinates between local and remote data
 * sources.
 * Uses DataSource interfaces for dependency inversion (SOLID - D).
 */
public class MealRepository implements MealRepositoryInterface {

    private static volatile MealRepository instance;
    private final MealRemoteDataSource remoteDataSource;
    private final MealLocalDataSource localDataSource;
    private final FirebaseSyncHelper syncHelper;

    private final Context context;

    private MealRepository(Context context) {
        this.context = context.getApplicationContext();
        // Create data sources using implementations
        remoteDataSource = new MealRemoteDataSourceImpl(
                Network.getInstance().getMealApiService());

        AppDatabase database = AppDatabase.getInstance(context);
        localDataSource = new MealLocalDataSourceImpl(
                database.favoriteMealDAO(),
                database.plannedMealDAO());

        syncHelper = FirebaseSyncHelper.getInstance();
    }

    public static synchronized MealRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MealRepository(context);
        }
        return instance;
    }

    // ============ Remote API Methods ============

    @Override
    public Single<MealResponse> getRandomMeal() {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.getRandomMeal();
    }

    @Override
    public Single<MealResponse> searchMealByName(String name) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.searchMealByName(name);
    }

    @Override
    public Single<MealResponse> getMealById(String id) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.getMealById(id);
    }

    @Override
    public Single<CategoryResponse> getAllCategories() {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.getAllCategories();
    }

    @Override
    public Single<AreaResponse> getAllAreas() {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.getAllAreas();
    }

    @Override
    public Single<IngredientResponse> getAllIngredients() {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.getAllIngredients();
    }

    @Override
    public Single<MealResponse> filterByCategory(String category) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.filterByCategory(category);
    }

    @Override
    public Single<MealResponse> filterByArea(String area) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.filterByArea(area);
    }

    @Override
    public Single<MealResponse> filterByIngredient(String ingredient) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.filterByIngredient(ingredient);
    }

    @Override
    public Single<MealResponse> searchMealsByName(String name) {
        if (!com.example.foodplanner.utils.NetworkUtils.isNetworkAvailable(context)) {
            return Single.error(new Exception("No Internet Connection"));
        }
        return remoteDataSource.searchMealByName(name);
    }

    // ============ Favorite Meals Methods ============

    @Override
    public Completable addFavorite(Meal meal, String userId) {
        FavoriteMeal favMeal = FavoriteMeal.fromMeal(meal, userId);
        return localDataSource.insertFavorite(favMeal)
                .andThen(backupFavorites(userId));
    }

    @Override
    public Completable removeFavorite(FavoriteMeal meal) {
        return localDataSource.deleteFavorite(meal)
                .andThen(backupFavorites(meal.getUserId()));
    }

    @Override
    public Completable removeFavoriteById(String mealId, String userId) {
        return localDataSource.deleteFavoriteById(mealId, userId)
                .andThen(backupFavorites(userId));
    }

    private Completable backupFavorites(String userId) {
        if (userId == null || userId.isEmpty())
            return Completable.complete();
        return localDataSource.getAllFavorites(userId)
                .firstOrError()
                .flatMapCompletable(favorites -> syncHelper.backupFavorites(userId, favorites))
                .onErrorComplete(); // Ignore sync errors to not fail the local operation
    }

    @Override
    public Flowable<List<FavoriteMeal>> getAllFavorites(String userId) {
        return localDataSource.getAllFavorites(userId);
    }

    @Override
    public Single<Boolean> isFavorite(String mealId, String userId) {
        return localDataSource.isFavorite(mealId, userId);
    }

    // ============ Planned Meals Methods ============

    @Override
    public Completable addPlannedMeal(Meal meal, String date, String mealType, String userId) {
        PlannedMeal plannedMeal = PlannedMeal.fromMeal(meal, date, mealType, userId);
        return localDataSource.insertPlannedMeal(plannedMeal)
                .andThen(backupPlannedMeals(userId));
    }

    @Override
    public Completable removePlannedMeal(PlannedMeal meal) {
        return localDataSource.deletePlannedMeal(meal)
                .andThen(backupPlannedMeals(meal.getUserId()));
    }

    private Completable backupPlannedMeals(String userId) {
        if (userId == null || userId.isEmpty())
            return Completable.complete();
        return localDataSource.getAllPlannedMeals(userId)
                .firstOrError()
                .flatMapCompletable(meals -> syncHelper.backupPlannedMeals(userId, meals))
                .onErrorComplete(); // Ignore sync errors
    }

    @Override
    public Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId) {
        return localDataSource.getAllPlannedMeals(userId);
    }

    @Override
    public Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId) {
        return localDataSource.getPlannedMealsByDate(date, userId);
    }

    @Override
    public Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId) {
        return localDataSource.getPlannedMealsForWeek(startDate, endDate, userId);
    }

    @Override
    public Flowable<List<PlannedMeal>> getPlannedMealsByDateRange(String startDate, String endDate, String userId) {
        return localDataSource.getPlannedMealsForWeek(startDate, endDate, userId);
    }

    // ============ Backup/Sync Methods ============

    @Override
    public Completable deleteAllFavorites(String userId) {
        return localDataSource.deleteAllFavorites(userId);
    }

    @Override
    public Completable insertAllFavorites(List<FavoriteMeal> meals) {
        return localDataSource.insertAllFavorites(meals);
    }

    @Override
    public Completable deleteAllPlannedMeals(String userId) {
        return localDataSource.deleteAllPlannedMeals(userId);
    }

    @Override
    public Completable insertAllPlannedMeals(List<PlannedMeal> meals) {
        return localDataSource.insertAllPlannedMeals(meals);
    }

    @Override
    public List<PlannedMeal> getAllPlannedMealsSync(String userId) {
        return localDataSource.getAllPlannedMealsSync(userId);
    }

    @Override
    public Completable insertPlannedMealDirectly(PlannedMeal meal) {
        return localDataSource.insertPlannedMeal(meal);
    }

    @Override
    public Completable addFavoriteDirectly(FavoriteMeal meal) {
        if (meal.getUserId() == null || meal.getUserId().isEmpty()) {
            return Completable.error(new IllegalArgumentException("User ID is required for favorites"));
        }
        return localDataSource.insertFavorite(meal);
    }

    @Override
    public Completable migrateAllData(String newUserId) {
        return localDataSource.migrateAllFavorites(newUserId)
                .andThen(localDataSource.deleteYieldedFavorites())
                .andThen(localDataSource.migrateAllPlannedMeals(newUserId))
                .andThen(localDataSource.deduplicatePlannedMeals());
    }

    @Override
    public Completable restoreAllData(String userId) {
        return syncHelper.restoreFavorites(userId)
                .flatMapCompletable(favorites -> {
                    if (favorites == null || favorites.isEmpty()) {
                        return Completable.complete();
                    }
                    for (FavoriteMeal meal : favorites) {
                        meal.setUserId(userId);
                    }
                    // Clear existing favorites to avoid duplicates, then insert new
                    return localDataSource.deleteAllFavorites(userId)
                            .andThen(localDataSource.insertAllFavorites(favorites));
                })
                .andThen(syncHelper.restorePlannedMeals(userId))
                .flatMapCompletable(plannedMeals -> {
                    if (plannedMeals == null || plannedMeals.isEmpty()) {
                        return Completable.complete();
                    }
                    for (PlannedMeal meal : plannedMeals) {
                        meal.setUserId(userId);
                    }
                    // Clear existing plans to avoid duplicates, then insert new
                    return localDataSource.deleteAllPlannedMeals(userId)
                            .andThen(localDataSource.insertAllPlannedMeals(plannedMeals));
                });
    }

    @Override
    public Completable clearUserData(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Completable.complete();
        }
        return localDataSource.deleteAllFavorites(userId)
                .andThen(localDataSource.deleteAllPlannedMeals(userId));
    }
}
