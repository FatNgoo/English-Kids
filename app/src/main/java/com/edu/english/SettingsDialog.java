package com.edu.english;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Settings Dialog with volume, sound effects toggle, and brightness controls
 * Styled to match Lingokid design
 */
public class SettingsDialog extends Dialog {

    private static final String PREFS_NAME = "english_kids_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_VOLUME = "volume";

    private SeekBar seekbarVolume;
    private SeekBar seekbarBrightness;
    private SwitchCompat switchSound;
    private SwitchCompat switchMusic;
    private TextView txtVolumeValue;
    private TextView txtBrightnessValue;

    private SharedPreferences preferences;
    private AudioManager audioManager;
    private OnSettingsChangedListener listener;

    public interface OnSettingsChangedListener {
        void onSoundToggled(boolean enabled);
        void onMusicToggled(boolean enabled);
        void onVolumeChanged(int volume);
        void onBrightnessChanged(int brightness);
    }

    public SettingsDialog(@NonNull Context context) {
        super(context, R.style.SettingsDialogTheme);
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setOnSettingsChangedListener(OnSettingsChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_settings);

        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        seekbarVolume = findViewById(R.id.seekbar_volume);
        seekbarBrightness = findViewById(R.id.seekbar_brightness);
        switchSound = findViewById(R.id.switch_sound);
        switchMusic = findViewById(R.id.switch_music);
        txtVolumeValue = findViewById(R.id.txt_volume_value);
        txtBrightnessValue = findViewById(R.id.txt_brightness_value);

        ImageButton btnClose = findViewById(R.id.btn_close_settings);
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void loadSettings() {
        // Load saved settings
        boolean soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, true);
        boolean musicEnabled = preferences.getBoolean(KEY_MUSIC_ENABLED, true);
        int savedVolume = preferences.getInt(KEY_VOLUME, 80);

        switchSound.setChecked(soundEnabled);
        switchMusic.setChecked(musicEnabled);
        seekbarVolume.setProgress(savedVolume);
        txtVolumeValue.setText(savedVolume + "%");

        // Load current brightness
        try {
            int brightness = Settings.System.getInt(
                    getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS
            );
            int brightnessPercent = (brightness * 100) / 255;
            seekbarBrightness.setProgress(brightnessPercent);
            txtBrightnessValue.setText(brightnessPercent + "%");
        } catch (Settings.SettingNotFoundException e) {
            seekbarBrightness.setProgress(70);
            txtBrightnessValue.setText("70%");
        }
    }

    private void setupListeners() {
        // Volume SeekBar
        seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtVolumeValue.setText(progress + "%");
                if (fromUser) {
                    // Set system volume
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int volume = (progress * maxVolume) / 100;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

                    // Save preference
                    preferences.edit().putInt(KEY_VOLUME, progress).apply();

                    if (listener != null) {
                        listener.onVolumeChanged(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Brightness SeekBar
        seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Minimum brightness 10%
                if (progress < 10) progress = 10;
                txtBrightnessValue.setText(progress + "%");

                if (fromUser) {
                    // Change window brightness
                    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                    layoutParams.screenBrightness = progress / 100f;
                    getWindow().setAttributes(layoutParams);

                    if (listener != null) {
                        listener.onBrightnessChanged(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Sound Effects Toggle
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_SOUND_ENABLED, isChecked).apply();
            if (listener != null) {
                listener.onSoundToggled(isChecked);
            }
        });

        // Music Toggle
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_MUSIC_ENABLED, isChecked).apply();
            if (listener != null) {
                listener.onMusicToggled(isChecked);
            }
        });
    }

    /**
     * Get sound enabled state
     */
    public static boolean isSoundEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    /**
     * Get music enabled state
     */
    public static boolean isMusicEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    /**
     * Get saved volume
     */
    public static int getSavedVolume(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_VOLUME, 80);
    }
}
