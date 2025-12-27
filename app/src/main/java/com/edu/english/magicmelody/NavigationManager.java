package com.edu.english.magicmelody;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.edu.english.magicmelody.ui.GameActivity;

/**
 * 🧭 Navigation Manager
 * 
 * Centralized navigation between screens:
 * - Type-safe navigation methods
 * - Intent extras management
 * - Transition animations
 * - Back stack management
 */
public class NavigationManager {
    
    private static final String TAG = "NavigationManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCREEN DEFINITIONS
    // ═══════════════════════════════════════════════════════════════
    
    public enum Screen {
        SPLASH,
        HOME,
        WORLD_MAP,
        LESSON_LIST,
        GAMEPLAY,
        BOSS_BATTLE,
        MAGIC_NOTEBOOK,
        ACHIEVEMENTS,
        SETTINGS,
        PLAYER_SELECT,
        PLAYER_PROFILE
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 SINGLETON
    // ═══════════════════════════════════════════════════════════════
    
    private static NavigationManager instance;
    
    public static synchronized NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    private NavigationManager() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏠 HOME NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to Home screen
     */
    public void navigateToHome(Context context) {
        navigateToHome(context, true);
    }
    
    public void navigateToHome(Context context, boolean clearStack) {
        // Intent intent = new Intent(context, HomeActivity.class);
        // if (clearStack) {
        //     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // }
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Home (clearStack=" + clearStack + ")");
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗺️ WORLD MAP NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to World Map
     */
    public void navigateToWorldMap(Context context) {
        // Intent intent = new Intent(context, WorldMapActivity.class);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to World Map");
    }
    
    /**
     * Navigate to specific world
     */
    public void navigateToWorld(Context context, long worldId) {
        // Intent intent = new Intent(context, WorldMapActivity.class);
        // intent.putExtra("world_id", worldId);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to World: " + worldId);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📚 LESSON NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to Lesson List for a world
     */
    public void navigateToLessonList(Context context, long worldId) {
        // Intent intent = new Intent(context, LessonListActivity.class);
        // intent.putExtra("world_id", worldId);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Lesson List for World: " + worldId);
    }
    
    /**
     * Navigate to Lesson Detail
     */
    public void navigateToLessonDetail(Context context, long lessonId) {
        // Intent intent = new Intent(context, LessonDetailActivity.class);
        // intent.putExtra("lesson_id", lessonId);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Lesson Detail: " + lessonId);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAMEPLAY NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to Gameplay
     */
    public void navigateToGameplay(Context context, long lessonId, long worldId, int difficulty) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_LESSON_ID, lessonId);
        intent.putExtra(GameActivity.EXTRA_WORLD_ID, worldId);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        context.startActivity(intent);
        Log.d(TAG, "Navigate to Gameplay: lesson=" + lessonId + ", world=" + worldId);
    }
    
    /**
     * Quick play - start gameplay with default settings
     */
    public void quickPlay(Context context, long lessonId) {
        navigateToGameplay(context, lessonId, 1, 1);
    }
    
    /**
     * Navigate to Boss Battle
     */
    public void navigateToBossBattle(Context context, long worldId, int bossId) {
        // Intent intent = new Intent(context, BossBattleActivity.class);
        // intent.putExtra("world_id", worldId);
        // intent.putExtra("boss_id", bossId);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Boss Battle: world=" + worldId + ", boss=" + bossId);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📔 FEATURES NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to Magic Notebook
     */
    public void navigateToMagicNotebook(Context context) {
        // Intent intent = new Intent(context, MagicNotebookActivity.class);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Magic Notebook");
    }
    
    /**
     * Navigate to Achievements
     */
    public void navigateToAchievements(Context context) {
        // Intent intent = new Intent(context, AchievementsActivity.class);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Achievements");
    }
    
    /**
     * Navigate to Settings
     */
    public void navigateToSettings(Context context) {
        // Intent intent = new Intent(context, SettingsActivity.class);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Settings");
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 👤 PLAYER NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate to Player Select
     */
    public void navigateToPlayerSelect(Context context) {
        // Intent intent = new Intent(context, PlayerSelectActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Player Select");
    }
    
    /**
     * Navigate to Player Profile
     */
    public void navigateToPlayerProfile(Context context, long playerId) {
        // Intent intent = new Intent(context, PlayerProfileActivity.class);
        // intent.putExtra("player_id", playerId);
        // context.startActivity(intent);
        Log.d(TAG, "Navigate to Player Profile: " + playerId);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔙 BACK NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Navigate back
     */
    public void navigateBack(Activity activity) {
        activity.onBackPressed();
    }
    
    /**
     * Navigate back to specific screen
     */
    public void navigateBackTo(Activity activity, Screen screen) {
        // This would require tracking the back stack
        // For now, just go back
        activity.onBackPressed();
    }
    
    /**
     * Finish current activity with result
     */
    public void finishWithResult(Activity activity, int resultCode, Bundle data) {
        Intent resultIntent = new Intent();
        if (data != null) {
            resultIntent.putExtras(data);
        }
        activity.setResult(resultCode, resultIntent);
        activity.finish();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 TRANSITIONS
    // ═══════════════════════════════════════════════════════════════
    
    public enum Transition {
        SLIDE_LEFT,
        SLIDE_RIGHT,
        SLIDE_UP,
        SLIDE_DOWN,
        FADE,
        ZOOM,
        NONE
    }
    
    /**
     * Apply transition animation
     */
    public void applyTransition(Activity activity, Transition transition) {
        switch (transition) {
            case SLIDE_LEFT:
                // activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case SLIDE_RIGHT:
                // activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case FADE:
                // activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case NONE:
                activity.overridePendingTransition(0, 0);
                break;
            default:
                break;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔗 DEEP LINKS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Handle deep link navigation
     */
    public void handleDeepLink(Context context, String uri) {
        Log.d(TAG, "Handling deep link: " + uri);
        
        // Parse URI and navigate
        // Example: magicmelody://lesson/123
        // Example: magicmelody://world/1
        // Example: magicmelody://notebook
        
        if (uri == null) return;
        
        if (uri.contains("/lesson/")) {
            String lessonIdStr = uri.substring(uri.lastIndexOf("/") + 1);
            try {
                long lessonId = Long.parseLong(lessonIdStr);
                navigateToLessonDetail(context, lessonId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid lesson ID in deep link");
            }
        } else if (uri.contains("/world/")) {
            String worldIdStr = uri.substring(uri.lastIndexOf("/") + 1);
            try {
                long worldId = Long.parseLong(worldIdStr);
                navigateToWorld(context, worldId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid world ID in deep link");
            }
        } else if (uri.contains("/notebook")) {
            navigateToMagicNotebook(context);
        } else if (uri.contains("/achievements")) {
            navigateToAchievements(context);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ANALYTICS
    // ═══════════════════════════════════════════════════════════════
    
    private void trackNavigation(Screen from, Screen to) {
        Log.d(TAG, "Navigation: " + from + " -> " + to);
        // Analytics tracking would go here
    }
}
