package com.example.foodplanner.data.meal.model;

public class IngredientWithMeasure {
    private String ingredient;
    private String measure;

    public IngredientWithMeasure(String ingredient, String measure) {
        this.ingredient = ingredient;
        this.measure = measure != null ? measure : "";
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredientImageUrl() {
        return "https://www.themealdb.com/images/ingredients/" + ingredient + "-Small.png";
    }
}
