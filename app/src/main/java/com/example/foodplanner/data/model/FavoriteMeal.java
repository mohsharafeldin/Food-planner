package com.example.testfoodplanner.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_meals")
public class FavoriteMeal {
    @PrimaryKey
    @NonNull
    private String idMeal;
    private String strMeal;
    private String strCategory;
    private String strArea;
    private String strInstructions;
    private String strMealThumb;
    private String strYoutube;
    private String strTags;
    private String userId;

    // Store ingredients as concatenated string for simplicity
    private String ingredients;
    private String measures;

    public FavoriteMeal() {
    }

    public static FavoriteMeal fromMeal(Meal meal, String userId) {
        FavoriteMeal favMeal = new FavoriteMeal();
        favMeal.setIdMeal(meal.getIdMeal());
        favMeal.setStrMeal(meal.getStrMeal());
        favMeal.setStrCategory(meal.getStrCategory());
        favMeal.setStrArea(meal.getStrArea());
        favMeal.setStrInstructions(meal.getStrInstructions());
        favMeal.setStrMealThumb(meal.getStrMealThumb());
        favMeal.setStrYoutube(meal.getStrYoutube());
        favMeal.setStrTags(meal.getStrTags());
        favMeal.setUserId(userId);

        // Concatenate ingredients
        StringBuilder ingBuilder = new StringBuilder();
        StringBuilder measBuilder = new StringBuilder();
        for (IngredientWithMeasure iwm : meal.getIngredientsList()) {
            ingBuilder.append(iwm.getIngredient()).append("||");
            measBuilder.append(iwm.getMeasure()).append("||");
        }
        favMeal.setIngredients(ingBuilder.toString());
        favMeal.setMeasures(measBuilder.toString());

        return favMeal;
    }

    @NonNull
    public String getIdMeal() {
        return idMeal;
    }

    public void setIdMeal(@NonNull String idMeal) {
        this.idMeal = idMeal;
    }

    public String getStrMeal() {
        return strMeal;
    }

    public void setStrMeal(String strMeal) {
        this.strMeal = strMeal;
    }

    public String getStrCategory() {
        return strCategory;
    }

    public void setStrCategory(String strCategory) {
        this.strCategory = strCategory;
    }

    public String getStrArea() {
        return strArea;
    }

    public void setStrArea(String strArea) {
        this.strArea = strArea;
    }

    public String getStrInstructions() {
        return strInstructions;
    }

    public void setStrInstructions(String strInstructions) {
        this.strInstructions = strInstructions;
    }

    public String getStrMealThumb() {
        return strMealThumb;
    }

    public void setStrMealThumb(String strMealThumb) {
        this.strMealThumb = strMealThumb;
    }

    public String getStrYoutube() {
        return strYoutube;
    }

    public void setStrYoutube(String strYoutube) {
        this.strYoutube = strYoutube;
    }

    public String getStrTags() {
        return strTags;
    }

    public void setStrTags(String strTags) {
        this.strTags = strTags;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getMeasures() {
        return measures;
    }

    public void setMeasures(String measures) {
        this.measures = measures;
    }
}
