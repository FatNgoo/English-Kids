package com.edu.english.masterchef.data.model;

/**
 * Represents a player action during cooking scene
 * Used for step validation
 */
public class PlayerAction {
    private ActionType type;
    private String itemId;
    private String targetZone;
    private int tapCount;
    private int holdDuration; // in milliseconds
    private float gestureProgress; // for stir/shake (0.0 - 1.0)

    public enum ActionType {
        DROP,           // Drag and drop
        TAP_ITEM,       // Tap on item
        TAP_ZONE,       // Tap on zone
        HOLD_START,     // Start holding
        HOLD_PROGRESS,  // Holding in progress
        HOLD_COMPLETE,  // Hold completed
        HOLD_CANCEL,    // Hold cancelled
        STIR,           // Circular gesture
        SHAKE           // Shake gesture
    }

    public PlayerAction() {
    }

    public PlayerAction(ActionType type, String itemId, String targetZone) {
        this.type = type;
        this.itemId = itemId;
        this.targetZone = targetZone;
    }

    public PlayerAction(ActionType type, String itemId, String targetZone, int value) {
        this.type = type;
        this.itemId = itemId;
        this.targetZone = targetZone;
        
        if (type == ActionType.TAP_ITEM || type == ActionType.TAP_ZONE) {
            this.tapCount = value;
        } else if (type == ActionType.HOLD_COMPLETE || type == ActionType.HOLD_PROGRESS) {
            this.holdDuration = value;
        }
    }

    // Getters and Setters
    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTargetZone() {
        return targetZone;
    }

    public void setTargetZone(String targetZone) {
        this.targetZone = targetZone;
    }

    public int getTapCount() {
        return tapCount;
    }

    public void setTapCount(int tapCount) {
        this.tapCount = tapCount;
    }

    public int getHoldDuration() {
        return holdDuration;
    }

    public void setHoldDuration(int holdDuration) {
        this.holdDuration = holdDuration;
    }

    public float getGestureProgress() {
        return gestureProgress;
    }

    public void setGestureProgress(float gestureProgress) {
        this.gestureProgress = gestureProgress;
    }

    // Convenience methods for Scene3Fragment
    public void setActionType(String actionType) {
        // Convert string to ActionType enum
        switch (actionType) {
            case "DragDrop":
                this.type = ActionType.DROP;
                break;
            case "Tap":
                this.type = ActionType.TAP_ITEM;
                break;
            case "TapZone":
                this.type = ActionType.TAP_ZONE;
                break;
            case "Hold":
            case "HoldComplete":
                this.type = ActionType.HOLD_COMPLETE;
                break;
            case "Stir":
                this.type = ActionType.STIR;
                break;
            case "Continue":
            case "TimerComplete":
                // Timer steps always succeed - use TAP_ZONE as generic success
                this.type = ActionType.TAP_ZONE;
                break;
            default:
                this.type = ActionType.DROP;
        }
    }

    public void setZoneId(String zoneId) {
        this.targetZone = zoneId;
    }
}
