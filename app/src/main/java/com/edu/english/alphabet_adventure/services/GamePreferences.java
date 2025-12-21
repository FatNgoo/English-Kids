package com.edu.english.alphabet_adventure.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Service for persisting game preferences and mascot selection.
 */
public class GamePreferences {

    private static final String PREFS_NAME = "alphabet_adventure_prefs";
    private static final String KEY_MASCOT_ID = "mascot_id";
    private static final String KEY_IS_MUTED = "is_muted";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_WORDS_COMPLETED = "words_completed";

    private SharedPreferences preferences;

    public GamePreferences(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setSelectedMascot(int mascotId) {
        preferences.edit().putInt(KEY_MASCOT_ID, mascotId).apply();
    }

    public int getSelectedMascot() {
        return preferences.getInt(KEY_MASCOT_ID, 1); // Default to first mascot
    }

    public void setMuted(boolean muted) {
        preferences.edit().putBoolean(KEY_IS_MUTED, muted).apply();
    }

    public boolean isMuted() {
        return preferences.getBoolean(KEY_IS_MUTED, false);
    }

    public void setHighScore(int score) {
        int currentHigh = getHighScore();
        if (score > currentHigh) {
            preferences.edit().putInt(KEY_HIGH_SCORE, score).apply();
        }
    }

    public int getHighScore() {
        return preferences.getInt(KEY_HIGH_SCORE, 0);
    }

    public void incrementWordsCompleted() {
        int current = getWordsCompleted();
        preferences.edit().putInt(KEY_WORDS_COMPLETED, current + 1).apply();
    }

    public int getWordsCompleted() {
        return preferences.getInt(KEY_WORDS_COMPLETED, 0);
    }

    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
