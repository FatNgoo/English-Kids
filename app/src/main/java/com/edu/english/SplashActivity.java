package com.edu.english;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start animations
        startAnimations();

        // Navigate to MainActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Monster bounce animation
        ImageView monster = findViewById(R.id.monster_image);
        ObjectAnimator monsterScale = ObjectAnimator.ofFloat(monster, "scaleX", 0f, 1f);
        ObjectAnimator monsterScaleY = ObjectAnimator.ofFloat(monster, "scaleY", 0f, 1f);
        monsterScale.setDuration(800);
        monsterScaleY.setDuration(800);
        monsterScale.setInterpolator(new BounceInterpolator());
        monsterScaleY.setInterpolator(new BounceInterpolator());

        // Logo animation
        ImageView logo = findViewById(R.id.logo_image);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        ObjectAnimator logoTranslate = ObjectAnimator.ofFloat(logo, "translationY", 50f, 0f);
        logoAlpha.setDuration(600);
        logoTranslate.setDuration(600);
        logoAlpha.setStartDelay(400);
        logoTranslate.setStartDelay(400);

        // Tagline animation
        TextView tagline = findViewById(R.id.tagline);
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 0.9f);
        taglineAlpha.setDuration(500);
        taglineAlpha.setStartDelay(700);

        // Floating shapes animations
        animateFloatingShape(findViewById(R.id.shape1), 0, 2000);
        animateFloatingShape(findViewById(R.id.shape2), 200, 2200);
        animateFloatingShape(findViewById(R.id.shape3), 400, 1800);
        animateFloatingShape(findViewById(R.id.shape4), 100, 2100);
        animateFloatingShape(findViewById(R.id.shape5), 300, 1900);
        animateFloatingShape(findViewById(R.id.shape6), 150, 2300);
        animateFloatingShape(findViewById(R.id.shape7), 250, 2000);

        // Monster wave animation (continuous)
        ObjectAnimator monsterWave = ObjectAnimator.ofFloat(monster, "rotation", -5f, 5f);
        monsterWave.setDuration(500);
        monsterWave.setRepeatCount(ObjectAnimator.INFINITE);
        monsterWave.setRepeatMode(ObjectAnimator.REVERSE);
        monsterWave.setStartDelay(800);

        // Start all animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(monsterScale, monsterScaleY, logoAlpha, logoTranslate, taglineAlpha);
        animatorSet.start();
        monsterWave.start();
    }

    private void animateFloatingShape(View shape, int startDelay, int duration) {
        // Float up and down animation
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(shape, "translationY", 0f, -20f);
        floatAnim.setDuration(duration);
        floatAnim.setRepeatCount(ObjectAnimator.INFINITE);
        floatAnim.setRepeatMode(ObjectAnimator.REVERSE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.setStartDelay(startDelay);
        floatAnim.start();

        // Subtle rotation
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(shape, "rotation", 0f, 360f);
        rotateAnim.setDuration(duration * 4);
        rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnim.setStartDelay(startDelay);
        rotateAnim.start();
    }
}
