package com.edu.english.masterchef.data.model;

import java.util.List;

/**
 * Represents a dish/food item in Master Chef game
 */
public class Food {
    private String dishId;
    private String name;
    private String nameVietnamese; // For bilingual support
    private String imageRes; // drawable name (e.g., "dish_spaghetti")
    private String theme; // "italian", "chinese", "mexican", "vietnamese", etc.
    private List<String> ingredientIds;
    private String description;

    public Food() {
    }

    public Food(String dishId, String name, String nameVietnamese, String imageRes, 
                String theme, List<String> ingredientIds, String description) {
        this.dishId = dishId;
        this.name = name;
        this.nameVietnamese = nameVietnamese;
        this.imageRes = imageRes;
        this.theme = theme;
        this.ingredientIds = ingredientIds;
        this.description = description;
    }

    // Getters and Setters
    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameVietnamese() {
        return nameVietnamese;
    }

    public void setNameVietnamese(String nameVietnamese) {
        this.nameVietnamese = nameVietnamese;
    }

    public String getImageRes() {
        return imageRes;
    }

    public void setImageRes(String imageRes) {
        this.imageRes = imageRes;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<String> getIngredientIds() {
        return ingredientIds;
    }

    public void setIngredientIds(List<String> ingredientIds) {
        this.ingredientIds = ingredientIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
