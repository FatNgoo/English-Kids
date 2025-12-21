package com.edu.english.coloralchemy;

/**
 * Easing functions for smooth animations
 * Provides various interpolation methods for natural-feeling motion
 */
public class EasingFunctions {
    
    /**
     * Linear interpolation - constant speed
     */
    public static float linear(float t) {
        return t;
    }
    
    /**
     * Ease In Quadratic - starts slow, accelerates
     */
    public static float easeInQuad(float t) {
        return t * t;
    }
    
    /**
     * Ease Out Quadratic - starts fast, decelerates
     */
    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }
    
    /**
     * Ease In Out Quadratic - smooth start and end
     */
    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
    
    /**
     * Ease In Cubic - stronger acceleration
     */
    public static float easeInCubic(float t) {
        return t * t * t;
    }
    
    /**
     * Ease Out Cubic - stronger deceleration
     */
    public static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }
    
    /**
     * Ease In Out Cubic - smooth cubic
     */
    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }
    
    /**
     * Ease Out Bounce - bouncy ending effect
     */
    public static float easeOutBounce(float t) {
        if (t < (1 / 2.75f)) {
            return 7.5625f * t * t;
        } else if (t < (2 / 2.75f)) {
            t -= (1.5f / 2.75f);
            return 7.5625f * t * t + 0.75f;
        } else if (t < (2.5f / 2.75f)) {
            t -= (2.25f / 2.75f);
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= (2.625f / 2.75f);
            return 7.5625f * t * t + 0.984375f;
        }
    }
    
    /**
     * Ease In Bounce
     */
    public static float easeInBounce(float t) {
        return 1 - easeOutBounce(1 - t);
    }
    
    /**
     * Ease In Out Bounce
     */
    public static float easeInOutBounce(float t) {
        return t < 0.5f
            ? easeInBounce(t * 2) * 0.5f
            : easeOutBounce(t * 2 - 1) * 0.5f + 0.5f;
    }
    
    /**
     * Overshoot - goes past target then returns
     */
    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }
    
    /**
     * Ease In Back - anticipation effect
     */
    public static float easeInBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }
    
    /**
     * Ease In Out Back - anticipation and overshoot
     */
    public static float easeInOutBack(float t) {
        float c1 = 1.70158f;
        float c2 = c1 * 1.525f;
        return t < 0.5f
            ? ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
            : ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
    }
    
    /**
     * Elastic easing - spring-like effect
     */
    public static float easeOutElastic(float t) {
        if (t == 0 || t == 1) return t;
        float c4 = (2 * (float) Math.PI) / 3;
        return (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75f) * c4) + 1;
    }
    
    /**
     * Ease In Elastic
     */
    public static float easeInElastic(float t) {
        if (t == 0 || t == 1) return t;
        float c4 = (2 * (float) Math.PI) / 3;
        return -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75f) * c4);
    }
    
    /**
     * Spring damping effect for liquid simulation
     */
    public static float springDamp(float t, float damping) {
        return (float) (Math.exp(-damping * t) * Math.cos(2 * Math.PI * t));
    }
    
    /**
     * Smooth step for gradual transitions
     */
    public static float smoothStep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
    
    /**
     * Clamp value between min and max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation between two values
     */
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    
    /**
     * Interpolate a color between two colors
     */
    public static int lerpColor(int colorStart, int colorEnd, float t) {
        int aS = (colorStart >> 24) & 0xff;
        int rS = (colorStart >> 16) & 0xff;
        int gS = (colorStart >> 8) & 0xff;
        int bS = colorStart & 0xff;
        
        int aE = (colorEnd >> 24) & 0xff;
        int rE = (colorEnd >> 16) & 0xff;
        int gE = (colorEnd >> 8) & 0xff;
        int bE = colorEnd & 0xff;
        
        int a = (int) lerp(aS, aE, t);
        int r = (int) lerp(rS, rE, t);
        int g = (int) lerp(gS, gE, t);
        int b = (int) lerp(bS, bE, t);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
