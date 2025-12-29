package com.edu.english.masterchef.engine;

import com.edu.english.masterchef.data.model.CookingStep;
import com.edu.english.masterchef.data.model.PlayerAction;

/**
 * Validates player actions against cooking steps
 * Returns validation results for each action
 */
public class StepValidator {

    public enum ValidationStatus {
        SUCCESS,        // Action completed successfully
        INCORRECT,      // Wrong action
        IN_PROGRESS,    // Action in progress (e.g., holding)
        CANCELLED,      // Action cancelled
        INVALID         // Invalid state
    }

    public static class ValidationResult {
        private ValidationStatus status;
        private String message;

        public ValidationResult(ValidationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public ValidationStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return status == ValidationStatus.SUCCESS;
        }

        public boolean isValid() {
            return isSuccess();
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(ValidationStatus.SUCCESS, message);
        }

        public static ValidationResult incorrect(String message) {
            return new ValidationResult(ValidationStatus.INCORRECT, message);
        }

        public static ValidationResult inProgress(String message) {
            return new ValidationResult(ValidationStatus.IN_PROGRESS, message);
        }

        public static ValidationResult cancelled(String message) {
            return new ValidationResult(ValidationStatus.CANCELLED, message);
        }
    }

    /**
     * Validate a player action against a cooking step
     */
    public ValidationResult validate(CookingStep step, PlayerAction action) {
        if (step == null || action == null) {
            return new ValidationResult(ValidationStatus.INVALID, "Step or action is null");
        }

        String stepType = step.getStepType();
        
        switch (stepType) {
            case "DragDropToZone":
                return validateDragDrop(step, action);
                
            case "TapOnItem":
                return validateTapItem(step, action);
                
            case "TapOnZone":
                return validateTapZone(step, action);
                
            case "HoldToPour":
                return validateHold(step, action);
                
            case "StirGesture":
                return validateStir(step, action);
                
            case "TimerWait":
                return ValidationResult.success("Timer completed");
                
            case "PlateServe":
                return validateDragDrop(step, action); // Same as drag drop
                
            default:
                return new ValidationResult(ValidationStatus.INVALID, "Unknown step type: " + stepType);
        }
    }

    /**
     * Validate drag and drop action
     */
    private ValidationResult validateDragDrop(CookingStep step, PlayerAction action) {
        if (action.getType() != PlayerAction.ActionType.DROP) {
            return ValidationResult.incorrect("Not a drop action");
        }

        String expectedItem = step.getItemId();
        String expectedZone = step.getTargetZone();
        String actualItem = action.getItemId();
        String actualZone = action.getTargetZone();

        if (expectedItem != null && !expectedItem.equals(actualItem)) {
            return ValidationResult.incorrect("Wrong item: expected " + expectedItem + ", got " + actualItem);
        }

        if (expectedZone != null && !expectedZone.equals(actualZone)) {
            return ValidationResult.incorrect("Wrong zone: expected " + expectedZone + ", got " + actualZone);
        }

        return ValidationResult.success("Item placed correctly!");
    }

    /**
     * Validate tap on item
     */
    private ValidationResult validateTapItem(CookingStep step, PlayerAction action) {
        if (action.getType() != PlayerAction.ActionType.TAP_ITEM) {
            return ValidationResult.incorrect("Not a tap item action");
        }

        String expectedItem = step.getItemId();
        String actualItem = action.getItemId();

        if (expectedItem != null && !expectedItem.equals(actualItem)) {
            return ValidationResult.incorrect("Wrong item tapped");
        }

        int requiredTaps = step.getRequiredCount();
        int actualTaps = action.getTapCount();

        if (actualTaps >= requiredTaps) {
            return ValidationResult.success("Tapped enough times!");
        } else {
            return ValidationResult.inProgress("Keep tapping... (" + actualTaps + "/" + requiredTaps + ")");
        }
    }

    /**
     * Validate tap on zone
     */
    private ValidationResult validateTapZone(CookingStep step, PlayerAction action) {
        if (action.getType() != PlayerAction.ActionType.TAP_ZONE) {
            return ValidationResult.incorrect("Not a tap zone action");
        }

        String expectedZone = step.getTargetZone();
        String actualZone = action.getTargetZone();

        if (expectedZone != null && !expectedZone.equals(actualZone)) {
            return ValidationResult.incorrect("Wrong zone tapped");
        }

        int requiredTaps = step.getRequiredCount();
        int actualTaps = action.getTapCount();

        if (actualTaps >= requiredTaps) {
            return ValidationResult.success("Zone activated!");
        } else {
            return ValidationResult.inProgress("Keep tapping... (" + actualTaps + "/" + requiredTaps + ")");
        }
    }

    /**
     * Validate hold to pour action
     */
    private ValidationResult validateHold(CookingStep step, PlayerAction action) {
        if (action.getType() == PlayerAction.ActionType.HOLD_CANCEL) {
            return ValidationResult.cancelled("Hold cancelled");
        }

        if (action.getType() != PlayerAction.ActionType.HOLD_COMPLETE &&
            action.getType() != PlayerAction.ActionType.HOLD_PROGRESS) {
            return ValidationResult.incorrect("Not a hold action");
        }

        String expectedItem = step.getItemId();
        String actualItem = action.getItemId();

        if (expectedItem != null && !expectedItem.equals(actualItem)) {
            return ValidationResult.incorrect("Wrong item for holding");
        }

        int requiredMs = step.getRequiredHoldMs();
        int actualMs = action.getHoldDuration();

        if (action.getType() == PlayerAction.ActionType.HOLD_COMPLETE && actualMs >= requiredMs) {
            return ValidationResult.success("Poured successfully!");
        } else {
            float progress = Math.min(1f, (float) actualMs / requiredMs);
            return ValidationResult.inProgress("Pouring... " + (int)(progress * 100) + "%");
        }
    }

    /**
     * Validate stir gesture
     */
    private ValidationResult validateStir(CookingStep step, PlayerAction action) {
        if (action.getType() != PlayerAction.ActionType.STIR) {
            return ValidationResult.incorrect("Not a stir action");
        }

        String expectedZone = step.getTargetZone();
        String actualZone = action.getTargetZone();

        if (expectedZone != null && !expectedZone.equals(actualZone)) {
            return ValidationResult.incorrect("Stirring wrong zone");
        }

        float requiredProgress = step.getRequiredProgress();
        float actualProgress = action.getGestureProgress();

        if (actualProgress >= requiredProgress) {
            return ValidationResult.success("Stirred well!");
        } else {
            return ValidationResult.inProgress("Keep stirring... " + (int)(actualProgress * 100) + "%");
        }
    }
}
