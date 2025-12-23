package com.edu.english.masterchef.data;

/**
 * Types of cooking actions in the Master Chef game.
 * Extended with Oven, Steamer, Grill, Deep Fry, and other advanced actions.
 */
public enum ActionType {
    // ===== BASIC ACTIONS (existing) =====
    
    /** Drag ingredient/tool to a zone */
    DRAG_TO_ZONE("drag", "Drag to the zone"),
    
    /** Tap multiple times to cut (on cutting board) */
    TAP_TO_CUT("tap", "Tap to cut"),
    
    /** Tap multiple times to pound meat (with mallet) */
    TAP_TO_POUND("tap", "Tap to pound"),
    
    /** Swipe left-right or circular to stir (in pot with ladle) */
    SWIPE_TO_STIR("swipe", "Swipe to stir"),
    
    /** Drag pan left-right to shake/toss (for frying) */
    SHAKE_PAN("shake", "Shake the pan"),
    
    /** Long press to pour liquid (oil, water) */
    POUR("hold", "Hold to pour"),
    
    /** Wait for cooking timer */
    WAIT_COOK("wait", "Wait for cooking"),
    
    /** Drag finished dish to plate */
    SERVE("drag", "Serve the dish"),
    
    // ===== OVEN ACTIONS =====
    
    /** Hold button to preheat oven */
    OVEN_PREHEAT_HOLD("hold", "Hold to preheat"),
    
    /** Rotate knob to set temperature */
    ROTATE_KNOB_TEMP("rotate", "Turn the knob"),
    
    /** Rotate knob to set timer */
    ROTATE_KNOB_TIMER("rotate", "Set the timer"),
    
    /** Stop cooking at the right moment */
    TIMING_STOP("tap", "Stop at the right time"),
    
    // ===== STEAMER ACTIONS =====
    
    /** Start steaming after setup */
    STEAM_START("wait", "Let it steam"),
    
    /** Tap to vent steam */
    VENT_STEAM_TAP("tap", "Tap to vent"),
    
    /** Swipe to wipe fog/condensation */
    WIPE_FOG_SWIPE("swipe", "Swipe to wipe"),
    
    // ===== GRILL ACTIONS =====
    
    /** Hold to turn on grill */
    GRILL_ON_HOLD("hold", "Hold to turn on"),
    
    /** Swipe to flip food */
    GRILL_FLIP_SWIPE("swipe", "Swipe to flip"),
    
    /** Wait for searing/cooking */
    WAIT_SEAR("wait", "Wait for searing"),
    
    // ===== DEEP FRY ACTIONS =====
    
    /** Adjust slider to fill oil level */
    OIL_LEVEL_SLIDER("slider", "Fill to the line"),
    
    /** Wait for oil to heat up */
    WAIT_OIL_HEAT("wait", "Wait for oil to heat"),
    
    /** Shake basket while frying */
    SHAKE_BASKET("shake", "Shake the basket"),
    
    // ===== BOIL/SIMMER ACTIONS =====
    
    /** Adjust heat level with slider */
    HEAT_LEVEL_SLIDER("slider", "Adjust the heat"),
    
    /** Swipe to skim foam */
    SKIM_FOAM_SWIPE("swipe", "Skim the foam"),
    
    /** Wait for water to boil */
    WAIT_BOIL("wait", "Wait for boiling"),
    
    // ===== MIXING/PREP ACTIONS =====
    
    /** Circular swipe to whisk */
    WHISK_CIRCLES("circle", "Whisk in circles"),
    
    /** Drag repeatedly to knead dough */
    KNEAD_DRAG("drag", "Knead the dough"),
    
    /** Tap to crack egg */
    TAP_TO_CRACK("tap", "Tap to crack"),
    
    // ===== GARNISH/PLATING ACTIONS =====
    
    /** Drag items to snap points on plate */
    GARNISH_DRAG_SNAP("drag", "Place the garnish"),
    
    /** Shake to sprinkle seasoning */
    SEASON_SHAKE("shake", "Shake to season"),
    
    /** Tap to add drops/dollops */
    TAP_TO_DRIZZLE("tap", "Tap to drizzle");
    
    private final String gestureType;
    private final String hintText;
    
    ActionType(String gestureType, String hintText) {
        this.gestureType = gestureType;
        this.hintText = hintText;
    }
    
    /**
     * @return The type of gesture needed (tap, swipe, hold, drag, rotate, shake, slider, circle, wait)
     */
    public String getGestureType() {
        return gestureType;
    }
    
    /**
     * @return Short hint text for the user
     */
    public String getHintText() {
        return hintText;
    }
    
    /**
     * @return true if this action requires waiting/timing
     */
    public boolean isTimedAction() {
        return this == WAIT_COOK || this == WAIT_BOIL || this == WAIT_OIL_HEAT 
            || this == WAIT_SEAR || this == STEAM_START || this == TIMING_STOP;
    }
    
    /**
     * @return true if this action uses rotation gesture
     */
    public boolean isRotaryAction() {
        return this == ROTATE_KNOB_TEMP || this == ROTATE_KNOB_TIMER;
    }
    
    /**
     * @return true if this action uses slider
     */
    public boolean isSliderAction() {
        return this == OIL_LEVEL_SLIDER || this == HEAT_LEVEL_SLIDER;
    }
    
    /**
     * @return true if this action requires holding
     */
    public boolean isHoldAction() {
        return this == OVEN_PREHEAT_HOLD || this == GRILL_ON_HOLD || this == STEAM_START;
    }
    
    /**
     * @return true if this action is a wait action (automatic timer)
     */
    public boolean isWaitAction() {
        return this == WAIT_COOK || this == WAIT_BOIL || this == WAIT_OIL_HEAT || this == WAIT_SEAR;
    }
    
    /**
     * @return true if this action requires shake gesture
     */
    public boolean isShakeAction() {
        return this == SHAKE_PAN || this == SHAKE_BASKET || this == SEASON_SHAKE;
    }
}
