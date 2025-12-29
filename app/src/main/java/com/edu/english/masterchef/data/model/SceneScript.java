package com.edu.english.masterchef.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the scripts/data for each scene in a level
 */
public class SceneScript {
    // Scene 1: Ordering
    private List<DialogLine> dialogLines;

    // Scene 2: Shopping
    private List<String> requiredIngredientIds;
    private List<String> distractorIngredientIds;

    // Scene 3: Cooking
    private List<CookingStep> cookingSteps;

    public SceneScript() {
        this.dialogLines = new ArrayList<>();
        this.requiredIngredientIds = new ArrayList<>();
        this.distractorIngredientIds = new ArrayList<>();
        this.cookingSteps = new ArrayList<>();
    }

    // Getters and Setters
    public List<DialogLine> getDialogLines() {
        return dialogLines;
    }

    public void setDialogLines(List<DialogLine> dialogLines) {
        this.dialogLines = dialogLines;
    }

    public List<String> getRequiredIngredientIds() {
        return requiredIngredientIds;
    }

    public void setRequiredIngredientIds(List<String> requiredIngredientIds) {
        this.requiredIngredientIds = requiredIngredientIds;
    }

    public List<String> getDistractorIngredientIds() {
        return distractorIngredientIds;
    }

    public void setDistractorIngredientIds(List<String> distractorIngredientIds) {
        this.distractorIngredientIds = distractorIngredientIds;
    }

    public List<CookingStep> getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(List<CookingStep> cookingSteps) {
        this.cookingSteps = cookingSteps;
    }

    // Builder methods for fluent API
    public SceneScript addDialog(DialogLine line) {
        this.dialogLines.add(line);
        return this;
    }

    public SceneScript addRequiredIngredient(String id) {
        this.requiredIngredientIds.add(id);
        return this;
    }

    public SceneScript addDistractor(String id) {
        this.distractorIngredientIds.add(id);
        return this;
    }

    public SceneScript addCookingStep(CookingStep step) {
        this.cookingSteps.add(step);
        return this;
    }

    // Convenience methods
    public List<DialogLine> getDialog() {
        return dialogLines;
    }

    public int getTargetTime() {
        return 300; // Default 5 minutes in seconds
    }
}
