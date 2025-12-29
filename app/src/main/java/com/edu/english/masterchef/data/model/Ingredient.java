package com.edu.english.masterchef.data.model;

/**
 * Represents an ingredient in Master Chef game
 */
public class Ingredient {
    private String ingredientId;
    private String name;
    private String nameVietnamese; // For bilingual support
    private String iconRes; // drawable name (e.g., "ing_tomato")
    private String category; // "vegetable", "meat", "dairy", "spice", "grain", "oil"
    private boolean isDistractor; // true if this is a wrong ingredient in shopping scene

    public Ingredient() {
    }

    public Ingredient(String ingredientId, String name, String nameVietnamese, 
                     String iconRes, String category) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.nameVietnamese = nameVietnamese;
        this.iconRes = iconRes;
        this.category = category;
        this.isDistractor = false;
    }

    // Getters and Setters
    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
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

    public String getIconRes() {
        return iconRes;
    }

    public void setIconRes(String iconRes) {
        this.iconRes = iconRes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDistractor() {
        return isDistractor;
    }

    public void setDistractor(boolean distractor) {
        isDistractor = distractor;
    }

    // Convenience method to get image resource ID
    public int getImageRes() {
        // This should return the actual resource ID
        // For now return placeholder
        return com.edu.english.R.drawable.ic_placeholder;
    }
}
