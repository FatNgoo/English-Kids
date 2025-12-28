package com.edu.english.magicmelody.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

/**
 * 🎨 Animation Utilities for Magic Melody
 * 
 * Purpose: Provide reusable animations for gameplay, UI transitions, and evolution effects
 * Usage: Call static methods to animate views throughout the game
 */
public final class AnimationUtils {
    
    private AnimationUtils() {
        // Prevent instantiation
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🎵 NOTE ANIMATIONS (Rhythm Gameplay)
    // ══════════════════════════════════════════════════════════════════
    
    /**
     * Animate a note appearing on screen with scale and fade
     */
    public static void animateNoteAppear(View noteView, long duration) {
        noteView.setScaleX(0f);
        noteView.setScaleY(0f);
        noteView.setAlpha(0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(noteView, "scaleX", 0f, 1f),
            ObjectAnimator.ofFloat(noteView, "scaleY", 0f, 1f),
            ObjectAnimator.ofFloat(noteView, "alpha", 0f, 1f)
        );
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
        animatorSet.start();
    }
    
    /**
     * Animate perfect hit with burst effect
     */
    public static void animatePerfectHit(View noteView, Runnable onComplete) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(noteView, "scaleX", 1f, 1.5f, 0f),
            ObjectAnimator.ofFloat(noteView, "scaleY", 1f, 1.5f, 0f),
            ObjectAnimator.ofFloat(noteView, "alpha", 1f, 1f, 0f)
        );
        animatorSet.setDuration(Constants.ANIMATION_MEDIUM);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                noteView.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            }
        });
        animatorSet.start();
    }
    
    /**
     * Animate note miss with shake and fade
     */
    public static void animateNoteMiss(View noteView, Runnable onComplete) {
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Shake horizontally
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(noteView, "translationX", 
            0f, -10f, 10f, -10f, 10f, 0f);
        shakeX.setDuration(Constants.ANIMATION_SHORT);
        
        // Fade out
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(noteView, "alpha", 1f, 0f);
        fadeOut.setDuration(Constants.ANIMATION_MEDIUM);
        fadeOut.setStartDelay(Constants.ANIMATION_SHORT);
        
        animatorSet.playSequentially(shakeX, fadeOut);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                noteView.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            }
        });
        animatorSet.start();
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🌈 EVOLUTION ANIMATIONS (Gray → Color Transformation)
    // ══════════════════════════════════════════════════════════════════
    
    /**
     * Animate grayscale to color transformation
     * @param imageView The image to transform
     * @param fromSaturation 0 = grayscale, 1 = full color
     * @param toSaturation Target saturation
     * @param duration Animation duration in ms
     */
    public static void animateColorEvolution(ImageView imageView, 
                                             float fromSaturation, 
                                             float toSaturation, 
                                             long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(fromSaturation, toSaturation);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float saturation = (float) animation.getAnimatedValue();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(saturation);
            imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        });
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (toSaturation >= 1f) {
                    // Clear filter when fully colored
                    imageView.clearColorFilter();
                }
            }
        });
        
        animator.start();
    }
    
    /**
     * Make image grayscale instantly
     */
    public static void setGrayscale(ImageView imageView) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f);
        imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }
    
    /**
     * Remove grayscale filter
     */
    public static void setFullColor(ImageView imageView) {
        imageView.clearColorFilter();
    }
    
    /**
     * Animate bloom effect when evolution completes
     */
    public static void animateBloomEffect(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Glow pulse
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        pulseX.setRepeatCount(2);
        pulseY.setRepeatCount(2);
        
        animatorSet.playTogether(pulseX, pulseY);
        animatorSet.setDuration(Constants.ANIMATION_LONG);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🎮 UI ANIMATIONS
    // ══════════════════════════════════════════════════════════════════
    
    /**
     * Animate button press feedback
     */
    public static void animateButtonPress(View button) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(
            createScaleAnimator(button, 1f, 0.9f, Constants.ANIMATION_SHORT / 2),
            createScaleAnimator(button, 0.9f, 1f, Constants.ANIMATION_SHORT / 2)
        );
        animatorSet.start();
    }
    
    /**
     * Animate view entrance from bottom
     */
    public static void animateSlideInFromBottom(View view, long delay) {
        view.setTranslationY(200f);
        view.setAlpha(0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", 200f, 0f),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        );
        animatorSet.setDuration(Constants.ANIMATION_MEDIUM);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    /**
     * Animate view entrance with bounce
     */
    public static void animateBounceIn(View view, long delay) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
            ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        );
        animatorSet.setDuration(Constants.ANIMATION_LONG);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new BounceInterpolator());
        animatorSet.start();
    }
    
    /**
     * Animate floating effect (for world map elements)
     */
    public static void animateFloating(View view, long duration) {
        ObjectAnimator floatUp = ObjectAnimator.ofFloat(view, "translationY", 0f, -15f);
        floatUp.setDuration(duration);
        floatUp.setRepeatMode(ValueAnimator.REVERSE);
        floatUp.setRepeatCount(ValueAnimator.INFINITE);
        floatUp.setInterpolator(new AccelerateDecelerateInterpolator());
        floatUp.start();
    }
    
    /**
     * Animate score popup
     */
    public static void animateScorePopup(View scoreView, Runnable onComplete) {
        scoreView.setAlpha(1f);
        scoreView.setScaleX(0.5f);
        scoreView.setScaleY(0.5f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Pop in
        AnimatorSet popIn = new AnimatorSet();
        popIn.playTogether(
            ObjectAnimator.ofFloat(scoreView, "scaleX", 0.5f, 1.2f, 1f),
            ObjectAnimator.ofFloat(scoreView, "scaleY", 0.5f, 1.2f, 1f)
        );
        popIn.setDuration(Constants.ANIMATION_MEDIUM);
        
        // Float up and fade
        AnimatorSet floatUp = new AnimatorSet();
        floatUp.playTogether(
            ObjectAnimator.ofFloat(scoreView, "translationY", 0f, -50f),
            ObjectAnimator.ofFloat(scoreView, "alpha", 1f, 0f)
        );
        floatUp.setDuration(Constants.ANIMATION_LONG);
        floatUp.setStartDelay(Constants.ANIMATION_SHORT);
        
        animatorSet.playSequentially(popIn, floatUp);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        animatorSet.start();
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 👹 BOSS BATTLE ANIMATIONS
    // ══════════════════════════════════════════════════════════════════
    
    /**
     * Animate boss taking damage
     */
    public static void animateBossDamage(View bossView) {
        // Flash red and shake
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(bossView, "translationX",
            0f, -20f, 20f, -15f, 15f, -10f, 10f, 0f);
        shakeX.setDuration(Constants.ANIMATION_MEDIUM);
        
        // Flash effect
        ObjectAnimator flash = ObjectAnimator.ofFloat(bossView, "alpha", 1f, 0.5f, 1f, 0.5f, 1f);
        flash.setDuration(Constants.ANIMATION_MEDIUM);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shakeX, flash);
        animatorSet.start();
    }
    
    /**
     * Animate boss defeat
     */
    public static void animateBossDefeat(View bossView, Runnable onComplete) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(bossView, "scaleX", 1f, 1.2f, 0f),
            ObjectAnimator.ofFloat(bossView, "scaleY", 1f, 1.2f, 0f),
            ObjectAnimator.ofFloat(bossView, "rotation", 0f, 360f),
            ObjectAnimator.ofFloat(bossView, "alpha", 1f, 0f)
        );
        animatorSet.setDuration(Constants.ANIMATION_EVOLUTION);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        animatorSet.start();
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🔧 HELPER METHODS
    // ══════════════════════════════════════════════════════════════════
    
    private static AnimatorSet createScaleAnimator(View view, float from, float to, long duration) {
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playTogether(
            ObjectAnimator.ofFloat(view, "scaleX", from, to),
            ObjectAnimator.ofFloat(view, "scaleY", from, to)
        );
        scaleSet.setDuration(duration);
        return scaleSet;
    }
}
