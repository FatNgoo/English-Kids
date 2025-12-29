package com.edu.english.masterchef.ui.scene2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.masterchef.data.model.Ingredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel for Scene 2 (Supermarket Shopping)
 * Manages ingredient selection and cart validation
 */
public class Scene2ViewModel extends AndroidViewModel {

    private MutableLiveData<List<Ingredient>> availableIngredients = new MutableLiveData<>();
    private MutableLiveData<Set<String>> selectedIngredients = new MutableLiveData<>(new HashSet<>());
    private MutableLiveData<Boolean> isCartValid = new MutableLiveData<>(false);
    private MutableLiveData<String> validationMessage = new MutableLiveData<>();

    private Set<String> requiredIngredientIds = new HashSet<>();
    private int requiredCount = 0;

    public Scene2ViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Initialize with ingredients
     */
    public void initializeScene(List<Ingredient> allIngredients, List<String> requiredIds) {
        this.availableIngredients.setValue(allIngredients);
        this.requiredIngredientIds = new HashSet<>(requiredIds);
        this.requiredCount = requiredIds.size();
        
        validateCart();
    }

    /**
     * Toggle ingredient selection
     */
    public void toggleIngredient(String ingredientId) {
        Set<String> currentSelection = selectedIngredients.getValue();
        if (currentSelection == null) {
            currentSelection = new HashSet<>();
        }

        if (currentSelection.contains(ingredientId)) {
            currentSelection.remove(ingredientId);
        } else {
            currentSelection.add(ingredientId);
        }

        selectedIngredients.setValue(currentSelection);
        validateCart();
    }

    /**
     * Clear all selections
     */
    public void clearCart() {
        selectedIngredients.setValue(new HashSet<>());
        validateCart();
    }

    /**
     * Check if ingredient is selected
     */
    public boolean isSelected(String ingredientId) {
        Set<String> selected = selectedIngredients.getValue();
        return selected != null && selected.contains(ingredientId);
    }

    /**
     * Validate cart contents
     */
    private void validateCart() {
        Set<String> selected = selectedIngredients.getValue();
        if (selected == null || selected.isEmpty()) {
            isCartValid.setValue(false);
            validationMessage.setValue("");
            return;
        }

        int selectedCount = selected.size();
        
        // Check if correct count
        if (selectedCount < requiredCount) {
            isCartValid.setValue(false);
            validationMessage.setValue("Need " + (requiredCount - selectedCount) + " more items");
            return;
        }

        if (selectedCount > requiredCount) {
            isCartValid.setValue(false);
            validationMessage.setValue("Too many items! Remove " + (selectedCount - requiredCount));
            return;
        }

        // Check if all required ingredients are selected
        if (!selected.containsAll(requiredIngredientIds)) {
            isCartValid.setValue(false);
            validationMessage.setValue("Wrong ingredients! Check your recipe.");
            return;
        }

        // Valid!
        isCartValid.setValue(true);
        validationMessage.setValue("Perfect! Ready to checkout.");
    }

    /**
     * Get cart count text
     */
    public String getCartCountText() {
        Set<String> selected = selectedIngredients.getValue();
        int count = selected != null ? selected.size() : 0;
        return count + " / " + requiredCount + " items";
    }

    // Getters for LiveData
    public LiveData<List<Ingredient>> getAvailableIngredients() {
        return availableIngredients;
    }

    public LiveData<List<Ingredient>> getAllIngredients() {
        return availableIngredients;
    }

    public LiveData<Set<String>> getSelectedIngredients() {
        return selectedIngredients;
    }

    public LiveData<List<String>> getSelectedIngredientIds() {
        MutableLiveData<List<String>> ids = new MutableLiveData<>();
        Set<String> selected = selectedIngredients.getValue();
        if (selected != null) {
            ids.setValue(new ArrayList<>(selected));
        } else {
            ids.setValue(new ArrayList<>());
        }
        return ids;
    }

    public List<String> getRequiredIngredientIds() {
        return new ArrayList<>(requiredIngredientIds);
    }

    public LiveData<Boolean> isCartValid() {
        return isCartValid;
    }

    public LiveData<String> getValidationMessage() {
        return validationMessage;
    }

    public boolean checkoutCart() {
        Boolean valid = isCartValid.getValue();
        return valid != null && valid;
    }
}
