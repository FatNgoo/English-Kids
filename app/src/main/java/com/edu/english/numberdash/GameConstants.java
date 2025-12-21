package com.edu.english.numberdash;

/**
 * Game constants for Number Dash Race
 * Contains all configuration values for the game
 */
public class GameConstants {
    
    // Target FPS
    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_NS = 1000000000L / TARGET_FPS;
    public static final float FRAME_TIME_MS = 1000f / TARGET_FPS;
    
    // Game states
    public static final int STATE_MENU = 0;
    public static final int STATE_CHARACTER_SELECT = 1;
    public static final int STATE_COUNTDOWN = 2;
    public static final int STATE_RACING = 3;
    public static final int STATE_QUESTION = 4;
    public static final int STATE_RESULT = 5;
    
    // Race settings
    public static final int TOTAL_QUESTIONS = 6;
    public static final float RACE_DISTANCE = 10000f; // Virtual race distance
    public static final float BASE_SPEED = 150f; // Base pixels per second
    public static final float BOOST_SPEED = 300f; // Speed when boosted
    public static final float SLOW_SPEED = 80f; // Speed when slowed
    public static final float BOOST_DURATION = 800f; // ms
    public static final float SLOW_DURATION = 600f; // ms
    
    // Character settings
    public static final int CHARACTER_WIDTH = 120;
    public static final int CHARACTER_HEIGHT = 120;
    public static final float CHARACTER_START_X = 0.15f; // % of screen width
    public static final float CHARACTER_Y = 0.73f; // % of screen height (on track)
    
    // Animation frame rates
    public static final float IDLE_FRAME_DURATION = 200f; // ms per frame
    public static final float RUN_FRAME_DURATION = 80f; // ms per frame
    public static final float BOOST_FRAME_DURATION = 60f; // ms per frame
    public static final float STUMBLE_FRAME_DURATION = 100f; // ms per frame
    
    // Parallax layer speeds (relative to base speed)
    public static final float BG_FAR_SPEED_RATIO = 0.2f;
    public static final float BG_MID_SPEED_RATIO = 0.5f;
    public static final float BG_NEAR_SPEED_RATIO = 0.8f;
    public static final float TRACK_SPEED_RATIO = 1.0f;
    
    // Question intervals (distance traveled before next question)
    public static final float QUESTION_INTERVAL = 1500f;
    public static final float FIRST_QUESTION_DELAY = 1000f;
    
    // UI sizes
    public static final int ANSWER_BUTTON_SIZE = 140;
    public static final int ANSWER_BUTTON_MARGIN = 40;
    public static final float BUTTON_PRESS_SCALE = 0.9f;
    public static final float BUTTON_PRESS_DURATION = 100f; // ms
    
    // Colors (ARGB)
    public static final int COLOR_SKY_TOP = 0xFF87CEEB;
    public static final int COLOR_SKY_BOTTOM = 0xFFE0F7FA;
    public static final int COLOR_GRASS = 0xFF7CB342;
    public static final int COLOR_TRACK = 0xFFFFB74D;
    public static final int COLOR_WHITE = 0xFFFFFFFF;
    public static final int COLOR_BLACK = 0xFF000000;
    public static final int COLOR_CORRECT = 0xFF4CAF50;
    public static final int COLOR_WRONG = 0xFFE53935;
    public static final int COLOR_BUTTON_1 = 0xFF2196F3;
    public static final int COLOR_BUTTON_2 = 0xFF9C27B0;
    public static final int COLOR_BUTTON_3 = 0xFFFF9800;
    public static final int COLOR_SHADOW = 0x40000000;
    public static final int COLOR_OVERLAY = 0x80000000;
    
    // Star rating thresholds
    public static final int STARS_THREE_THRESHOLD = 5; // 5+ correct = 3 stars
    public static final int STARS_TWO_THRESHOLD = 3;   // 3-4 correct = 2 stars
    // Less than 3 = 1 star
    
    // Particle effects
    public static final int BOOST_PARTICLES = 15;
    public static final float PARTICLE_LIFETIME = 500f; // ms
    public static final float PARTICLE_SPEED = 200f;
    
    // Countdown
    public static final int COUNTDOWN_SECONDS = 3;
    public static final float COUNTDOWN_SCALE_START = 2.0f;
    public static final float COUNTDOWN_SCALE_END = 1.0f;
    
    // Easing constants
    public static final float OVERSHOOT_TENSION = 1.5f;
}
