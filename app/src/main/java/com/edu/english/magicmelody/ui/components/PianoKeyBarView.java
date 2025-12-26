package com.edu.english.magicmelody.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * PianoKeyBarView - Container for 7 piano keys (C-B)
 * Handles key press events and routing to game engine
 */
public class PianoKeyBarView extends LinearLayout implements BouncyPianoKeyView.OnKeyPressListener {
    
    private static final int NUM_KEYS = 7;
    
    private final List<BouncyPianoKeyView> keys = new ArrayList<>();
    private OnKeyPressedListener listener;
    
    public interface OnKeyPressedListener {
        void onKeyPressed(int lane);
        void onKeyReleased(int lane);
    }
    
    public PianoKeyBarView(Context context) {
        super(context);
        init();
    }
    
    public PianoKeyBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PianoKeyBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setOrientation(HORIZONTAL);
        setWeightSum(NUM_KEYS);
        
        // Create 7 piano keys
        for (int i = 0; i < NUM_KEYS; i++) {
            BouncyPianoKeyView key = new BouncyPianoKeyView(getContext());
            key.setNoteIndex(i);
            key.setOnKeyPressListener(this);
            
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
            params.setMargins(4, 8, 4, 8);
            key.setLayoutParams(params);
            
            keys.add(key);
            addView(key);
        }
    }
    
    @Override
    public void onKeyPressed(int noteIndex) {
        if (listener != null) {
            listener.onKeyPressed(noteIndex);
        }
    }
    
    @Override
    public void onKeyReleased(int noteIndex) {
        if (listener != null) {
            listener.onKeyReleased(noteIndex);
        }
    }
    
    /**
     * Set listener for key press events
     */
    public void setOnKeyPressedListener(OnKeyPressedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get a specific key by lane index
     */
    public BouncyPianoKeyView getKey(int lane) {
        if (lane >= 0 && lane < keys.size()) {
            return keys.get(lane);
        }
        return null;
    }
    
    /**
     * Highlight a specific lane
     */
    public void highlightLane(int lane, boolean highlight) {
        BouncyPianoKeyView key = getKey(lane);
        if (key != null) {
            key.setHighlighted(highlight);
        }
    }
    
    /**
     * Clear all highlights
     */
    public void clearHighlights() {
        for (BouncyPianoKeyView key : keys) {
            key.setHighlighted(false);
        }
    }
    
    /**
     * Simulate key press (for tutorial/demo)
     */
    public void simulateKeyPress(int lane) {
        BouncyPianoKeyView key = getKey(lane);
        if (key != null) {
            key.simulatePress();
        }
    }
    
    /**
     * Get number of keys
     */
    public int getKeyCount() {
        return keys.size();
    }
    
    /**
     * Enable/disable all keys
     */
    public void setKeysEnabled(boolean enabled) {
        for (BouncyPianoKeyView key : keys) {
            key.setEnabled(enabled);
            key.setAlpha(enabled ? 1f : 0.5f);
        }
    }
}
