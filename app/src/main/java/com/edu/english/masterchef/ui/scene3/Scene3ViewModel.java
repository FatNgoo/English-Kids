package com.edu.english.masterchef.ui.scene3;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.masterchef.data.model.CookingStep;
import com.edu.english.masterchef.data.model.SceneScript;

import java.util.List;

/**
 * ViewModel for Scene 3 (Kitchen Cooking)
 * Manages cooking step progression
 */
public class Scene3ViewModel extends AndroidViewModel {

    private MutableLiveData<Integer> currentStepIndex = new MutableLiveData<>(0);
    private MutableLiveData<CookingStep> currentStep = new MutableLiveData<>();
    private MutableLiveData<Boolean> isStepComplete = new MutableLiveData<>(false);
    
    private List<CookingStep> cookingSteps;

    public Scene3ViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Initialize with cooking steps
     */
    public void initializeScene(SceneScript script) {
        if (script == null) return;
        
        this.cookingSteps = script.getCookingSteps();
        
        if (cookingSteps != null && !cookingSteps.isEmpty()) {
            currentStepIndex.setValue(0);
            currentStep.setValue(cookingSteps.get(0));
        }
    }

    /**
     * Move to next step
     */
    public void nextStep() {
        if (cookingSteps == null || cookingSteps.isEmpty()) {
            isStepComplete.setValue(true);
            return;
        }

        int currentIndex = currentStepIndex.getValue() != null ? currentStepIndex.getValue() : 0;
        int nextIndex = currentIndex + 1;

        if (nextIndex < cookingSteps.size()) {
            currentStepIndex.setValue(nextIndex);
            currentStep.setValue(cookingSteps.get(nextIndex));
            isStepComplete.setValue(false);
        } else {
            // All steps complete - mark as finished
            currentStepIndex.setValue(nextIndex); // Set to total count
            isStepComplete.setValue(true);
        }
    }

    /**
     * Mark current step as complete
     */
    public void completeCurrentStep() {
        nextStep();
    }

    /**
     * Get total step count
     */
    public int getTotalSteps() {
        return cookingSteps != null ? cookingSteps.size() : 0;
    }

    /**
     * Get current step number (1-indexed)
     */
    public int getCurrentStepNumber() {
        return currentStepIndex.getValue() + 1;
    }

    /**
     * Check if all steps are complete
     */
    public boolean isAllStepsComplete() {
        if (cookingSteps == null) return true;
        int currentIndex = currentStepIndex.getValue();
        return currentIndex >= cookingSteps.size();
    }

    // Getters for LiveData
    public LiveData<Integer> getCurrentStepIndex() {
        return currentStepIndex;
    }

    public LiveData<CookingStep> getCurrentStep() {
        return currentStep;
    }

    public LiveData<Boolean> isStepComplete() {
        return isStepComplete;
    }
}
