package com.edu.english.numberdash;

/**
 * Easing functions for smooth animations
 * All functions take t (0-1) and return eased value (0-1)
 */
public class EasingUtils {
    
    /**
     * Linear interpolation (no easing)
     */
    public static float linear(float t) {
        return clamp(t);
    }
    
    /**
     * Ease in quadratic
     */
    public static float easeInQuad(float t) {
        t = clamp(t);
        return t * t;
    }
    
    /**
     * Ease out quadratic
     */
    public static float easeOutQuad(float t) {
        t = clamp(t);
        return 1 - (1 - t) * (1 - t);
    }
    
    /**
     * Ease in-out quadratic
     */
    public static float easeInOutQuad(float t) {
        t = clamp(t);
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }
    
    /**
     * Ease in cubic
     */
    public static float easeInCubic(float t) {
        t = clamp(t);
        return t * t * t;
    }
    
    /**
     * Ease out cubic
     */
    public static float easeOutCubic(float t) {
        t = clamp(t);
        return 1 - (float) Math.pow(1 - t, 3);
    }
    
    /**
     * Ease in-out cubic
     */
    public static float easeInOutCubic(float t) {
        t = clamp(t);
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
    
    /**
     * Overshoot ease out - bounces past target then settles
     */
    public static float easeOutOvershoot(float t) {
        return easeOutOvershoot(t, GameConstants.OVERSHOOT_TENSION);
    }
    
    public static float easeOutOvershoot(float t, float tension) {
        t = clamp(t);
        t = t - 1;
        return t * t * ((tension + 1) * t + tension) + 1;
    }
    
    /**
     * Bounce ease out - like a ball bouncing
     */
    public static float easeOutBounce(float t) {
        t = clamp(t);
        float n1 = 7.5625f;
        float d1 = 2.75f;
        
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5 / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625f / d1;
            return n1 * t * t + 0.984375f;
        }
    }
    
    /**
     * Elastic ease out - spring-like effect
     */
    public static float easeOutElastic(float t) {
        t = clamp(t);
        if (t == 0 || t == 1) return t;
        float c4 = (2 * (float) Math.PI) / 3;
        return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75f) * c4) + 1);
    }
    
    /**
     * Back ease in-out - slight overshoot on both ends
     */
    public static float easeInOutBack(float t) {
        t = clamp(t);
        float c1 = 1.70158f;
        float c2 = c1 * 1.525f;
        
        return t < 0.5f
                ? (float) (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                : (float) (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
    }
    
    /**
     * Smooth step - S-curve, commonly used for smooth transitions
     */
    public static float smoothStep(float t) {
        t = clamp(t);
        return t * t * (3 - 2 * t);
    }
    
    /**
     * Smoother step - even smoother S-curve
     */
    public static float smootherStep(float t) {
        t = clamp(t);
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    /**
     * Interpolate between two values with easing
     */
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    
    /**
     * Clamp value between 0 and 1
     */
    public static float clamp(float t) {
        return Math.max(0, Math.min(1, t));
    }
    
    /**
     * Clamp value between min and max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Map a value from one range to another
     */
    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }
}
