package com.edu.english.masterchef.data;

import java.io.Serializable;
import java.util.List;

/**
 * Model representing a complete recipe.
 */
public class Recipe implements Serializable {
    
    private String id;
    private String nameEn;
    private String nameVi;
    private String description;
    private String thumbnailDrawable;
    private int difficulty; // 1-3 (Easy, Medium, Hard)
    private List<RecipeStep> steps;
    private List<String> allIngredientIds;
    private List<String> allToolIds;
    
    public Recipe() {}
    
    public Recipe(String id, String nameEn, String nameVi, String description,
                  String thumbnailDrawable, int difficulty, List<RecipeStep> steps,
                  List<String> allIngredientIds, List<String> allToolIds) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
        this.description = description;
        this.thumbnailDrawable = thumbnailDrawable;
        this.difficulty = difficulty;
        this.steps = steps;
        this.allIngredientIds = allIngredientIds;
        this.allToolIds = allToolIds;
    }
    
    // Getters
    public String getId() { return id; }
    public String getNameEn() { return nameEn; }
    public String getNameVi() { return nameVi; }
    public String getDescription() { return description; }
    public String getThumbnailDrawable() { return thumbnailDrawable; }
    public int getDifficulty() { return difficulty; }
    public List<RecipeStep> getSteps() { return steps; }
    public List<String> getAllIngredientIds() { return allIngredientIds; }
    public List<String> getAllToolIds() { return allToolIds; }
    
    public int getTotalSteps() {
        return steps != null ? steps.size() : 0;
    }
    
    public RecipeStep getStep(int index) {
        if (steps != null && index >= 0 && index < steps.size()) {
            return steps.get(index);
        }
        return null;
    }
    
    public String getDifficultyText() {
        switch (difficulty) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            default: return "Easy";
        }
    }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }
    public void setDescription(String description) { this.description = description; }
    public void setThumbnailDrawable(String thumbnailDrawable) { this.thumbnailDrawable = thumbnailDrawable; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public void setSteps(List<RecipeStep> steps) { this.steps = steps; }
    public void setAllIngredientIds(List<String> allIngredientIds) { this.allIngredientIds = allIngredientIds; }
    public void setAllToolIds(List<String> allToolIds) { this.allToolIds = allToolIds; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id != null && id.equals(recipe.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
