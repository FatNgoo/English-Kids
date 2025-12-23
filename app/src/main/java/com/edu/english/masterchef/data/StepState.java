package com.edu.english.masterchef.data;

/**
 * State of the current recipe step.
 */
public enum StepState {
    /** Waiting for player to drag required item to zone */
    WAITING_ITEM,
    
    /** Item placed, ready to perform action */
    READY_ACTION,
    
    /** Action in progress (cutting, stirring, etc.) */
    ACTION_IN_PROGRESS,
    
    /** Action completed, step done */
    STEP_DONE,
    
    /** Speaking practice phase */
    SPEAKING_CHECK,
    
    /** Entire recipe completed */
    RECIPE_DONE
}
