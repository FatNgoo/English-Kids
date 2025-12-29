package com.edu.english.masterchef.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Manager for Text-to-Speech functionality
 * Supports karaoke mode with word highlighting
 */
public class TTSManager {
    
    private static TTSManager instance;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private OnTTSListener listener;
    private Handler mainHandler;
    
    // Karaoke mode
    private TextView karaokeTextView;
    private String currentText;
    private int highlightColor;
    private int normalColor;
    private Handler karaokeHandler;
    private boolean karaokeMode = false;

    public interface OnTTSListener {
        void onTTSInitialized(boolean success);
        void onSpeakStart(String utteranceId);
        void onSpeakDone(String utteranceId);
        void onSpeakError(String utteranceId);
    }

    private TTSManager(Context context) {
        mainHandler = new Handler(Looper.getMainLooper());
        karaokeHandler = new Handler(Looper.getMainLooper());
        
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = false;
                    if (listener != null) {
                        listener.onTTSInitialized(false);
                    }
                } else {
                    isInitialized = true;
                    tts.setSpeechRate(0.9f); // Slightly slower for kids
                    setupUtteranceListener();
                    if (listener != null) {
                        listener.onTTSInitialized(true);
                    }
                }
            } else {
                isInitialized = false;
                if (listener != null) {
                    listener.onTTSInitialized(false);
                }
            }
        });
    }

    public static synchronized TTSManager getInstance(Context context) {
        if (instance == null) {
            instance = new TTSManager(context);
        }
        return instance;
    }

    private void setupUtteranceListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSpeakStart(utteranceId);
                    }
                    if (karaokeMode && karaokeTextView != null) {
                        startKaraoke();
                    }
                });
            }

            @Override
            public void onDone(String utteranceId) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSpeakDone(utteranceId);
                    }
                    if (karaokeMode && karaokeTextView != null) {
                        resetKaraoke();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSpeakError(utteranceId);
                    }
                });
            }
        });
    }

    /**
     * Speak text with optional utterance ID
     */
    public void speak(String text, String utteranceId) {
        if (!isInitialized || text == null || text.isEmpty()) {
            return;
        }
        
        // Use newer API (Bundle instead of HashMap)
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
    }

    /**
     * Speak text with karaoke highlighting
     */
    public void speakWithKaraoke(String text, TextView textView, int highlightColor, int normalColor) {
        speakWithKaraoke(text, textView, highlightColor, normalColor, false);
    }

    /**
     * Speak text with karaoke highlighting (with option for double read)
     * @param doubleRead if true, read twice: slow (0.7x) then normal (1.0x)
     */
    public void speakWithKaraoke(String text, TextView textView, int highlightColor, int normalColor, boolean doubleRead) {
        this.karaokeTextView = textView;
        this.currentText = text;
        this.highlightColor = highlightColor;
        this.normalColor = normalColor;
        this.karaokeMode = true;
        
        // Set initial text
        textView.setText(text);
        textView.setTextColor(normalColor);
        
        if (doubleRead) {
            // First read: slow (0.7x speed)
            tts.setSpeechRate(0.7f);
            speak(text, "karaoke_slow");
            
            // Schedule second read after first completes (estimate duration)
            int estimatedDuration = text.split("\\s+").length * 600; // ~600ms per word at 0.7x
            karaokeHandler.postDelayed(() -> {
                // Second read: normal (1.0x speed)
                tts.setSpeechRate(1.0f);
                speak(text, "karaoke_normal");
            }, estimatedDuration);
        } else {
            speak(text, "karaoke");
        }
    }

    /**
     * Start karaoke word highlighting
     */
    private void startKaraoke() {
        if (currentText == null || karaokeTextView == null) {
            return;
        }

        String[] words = currentText.split(" ");
        int wordDelay = estimateWordDuration(currentText, words.length);

        karaokeHandler.post(new Runnable() {
            int currentWordIndex = 0;

            @Override
            public void run() {
                if (currentWordIndex < words.length && karaokeMode) {
                    highlightWord(currentWordIndex, words);
                    currentWordIndex++;
                    karaokeHandler.postDelayed(this, wordDelay);
                } else {
                    resetKaraoke();
                }
            }
        });
    }

    /**
     * Highlight a specific word in the text
     */
    private void highlightWord(int wordIndex, String[] words) {
        if (karaokeTextView == null) return;

        SpannableString spannableString = new SpannableString(currentText);
        
        // Find word position in original text
        int currentPos = 0;
        for (int i = 0; i < words.length; i++) {
            int wordStart = currentText.indexOf(words[i], currentPos);
            int wordEnd = wordStart + words[i].length();
            
            if (i == wordIndex) {
                // Highlight current word
                ForegroundColorSpan highlightSpan = new ForegroundColorSpan(highlightColor);
                spannableString.setSpan(highlightSpan, wordStart, wordEnd, 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            currentPos = wordEnd;
        }
        
        karaokeTextView.setText(spannableString);
    }

    /**
     * Estimate duration per word (milliseconds)
     */
    private int estimateWordDuration(String text, int wordCount) {
        // Average speaking rate: ~150 words per minute at 0.9x speed
        // = 150 / 60 = 2.5 words per second with 0.9 rate = ~2.25 wps
        // = ~445ms per word
        int textLength = text.length();
        int estimatedTotalMs = (int) (textLength * 50 / 0.9f); // rough estimate
        return wordCount > 0 ? estimatedTotalMs / wordCount : 500;
    }

    /**
     * Reset karaoke highlighting
     */
    private void resetKaraoke() {
        karaokeMode = false;
        if (karaokeTextView != null) {
            karaokeTextView.setTextColor(normalColor);
        }
        karaokeHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Stop current speech
     */
    public void stop() {
        if (tts != null) {
            tts.stop();
        }
        resetKaraoke();
    }

    /**
     * Check if TTS is currently speaking
     */
    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    /**
     * Set speech rate
     */
    public void setSpeechRate(float rate) {
        if (tts != null) {
            tts.setSpeechRate(rate);
        }
    }

    /**
     * Set TTS listener
     */
    public void setOnTTSListener(OnTTSListener listener) {
        this.listener = listener;
    }

    /**
     * Shutdown TTS engine
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        karaokeHandler.removeCallbacksAndMessages(null);
        isInitialized = false;
    }

    public void setListener(OnTTSListener listener) {
        this.listener = listener;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
