package com.edu.english.masterchef.ui.scene1;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.masterchef.data.model.DialogLine;
import com.edu.english.masterchef.data.model.SceneScript;

import java.util.List;

/**
 * ViewModel for Scene 1 (Restaurant Ordering)
 * Manages dialog progression and text display
 */
public class Scene1ViewModel extends AndroidViewModel {

    private MutableLiveData<Integer> currentDialogIndex = new MutableLiveData<>(0);
    private MutableLiveData<DialogLine> currentDialog = new MutableLiveData<>();
    private MutableLiveData<Boolean> isComplete = new MutableLiveData<>(false);

    private List<DialogLine> dialogLines;

    public Scene1ViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Initialize with scene script
     */
    public void initializeScene(SceneScript script) {
        if (script == null) return;
        
        this.dialogLines = script.getDialog();
        
        if (dialogLines != null && !dialogLines.isEmpty()) {
            currentDialogIndex.setValue(0);
            currentDialog.setValue(dialogLines.get(0));
        }
    }

    /**
     * Move to next dialog line
     */
    public void nextDialog() {
        if (dialogLines == null || dialogLines.isEmpty()) return;

        int currentIndex = currentDialogIndex.getValue();
        int nextIndex = currentIndex + 1;

        if (nextIndex < dialogLines.size()) {
            currentDialogIndex.setValue(nextIndex);
            currentDialog.setValue(dialogLines.get(nextIndex));
        } else {
            // All dialogs complete
            isComplete.setValue(true);
        }
    }

    /**
     * Skip all dialogs
     */
    public void skipDialogs() {
        isComplete.setValue(true);
    }

    /**
     * Check if there are more dialogs
     */
    public boolean hasMoreDialogs() {
        if (dialogLines == null) return false;
        int currentIndex = currentDialogIndex.getValue();
        return currentIndex < dialogLines.size() - 1;
    }

    // Getters for LiveData
    public LiveData<Integer> getCurrentDialogIndex() {
        return currentDialogIndex;
    }

    public LiveData<DialogLine> getCurrentDialog() {
        return currentDialog;
    }

    public LiveData<Boolean> isComplete() {
        return isComplete;
    }
}
