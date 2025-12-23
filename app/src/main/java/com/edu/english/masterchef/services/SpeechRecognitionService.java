package com.edu.english.masterchef.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.content.ContextCompat;

import com.edu.english.masterchef.utils.TextNormalizer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Speech recognition service for Master Chef game.
 * Handles listening to user speech and matching against expected phrases.
 */
public class SpeechRecognitionService {
    
    public interface SpeechListener {
        void onReadyForSpeech();
        void onListening();
        void onSpeechResult(String spokenText, boolean matchesExpected);
        void onPartialResult(String partialText);
        void onError(String errorMessage, boolean canRetry);
        void onSpeechNotAvailable();
    }
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private SpeechListener listener;
    private Handler handler;
    private String expectedPhrase;
    private boolean isListening;
    private boolean isAvailable;
    
    public SpeechRecognitionService(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        this.isListening = false;
        checkAvailability();
    }
    
    public void setListener(SpeechListener listener) {
        this.listener = listener;
    }
    
    private void checkAvailability() {
        isAvailable = SpeechRecognizer.isRecognitionAvailable(context);
        if (!isAvailable && listener != null) {
            listener.onSpeechNotAvailable();
        }
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Initialize the speech recognizer. Call after permission is granted.
     */
    public void initialize() {
        if (!isAvailable) {
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
            return;
        }
        
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                handler.post(() -> {
                    if (listener != null) listener.onReadyForSpeech();
                });
            }
            
            @Override
            public void onBeginningOfSpeech() {
                handler.post(() -> {
                    if (listener != null) listener.onListening();
                });
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Volume level changed - could use for UI feedback
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {}
            
            @Override
            public void onEndOfSpeech() {
                isListening = false;
            }
            
            @Override
            public void onError(int error) {
                isListening = false;
                handler.post(() -> handleError(error));
            }
            
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                handler.post(() -> handleResults(results));
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partial = matches.get(0);
                    handler.post(() -> {
                        if (listener != null) listener.onPartialResult(partial);
                    });
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }
    
    /**
     * Start listening for the expected phrase.
     */
    public void startListening(String expectedPhrase) {
        if (!isAvailable || speechRecognizer == null) {
            if (listener != null) listener.onSpeechNotAvailable();
            return;
        }
        
        if (!hasPermission()) {
            if (listener != null) listener.onError("Microphone permission required", false);
            return;
        }
        
        if (isListening) {
            stopListening();
        }
        
        this.expectedPhrase = expectedPhrase;
        this.isListening = true;
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        
        try {
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            isListening = false;
            if (listener != null) {
                listener.onError("Could not start listening", true);
            }
        }
    }
    
    /**
     * Stop listening.
     */
    public void stopListening() {
        isListening = false;
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    /**
     * Cancel current recognition.
     */
    public void cancel() {
        stopListening();
    }
    
    private void handleResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION);
        
        if (matches == null || matches.isEmpty()) {
            if (listener != null) {
                listener.onError("I didn't hear anything. Try again!", true);
            }
            return;
        }
        
        // Check each result for a match
        for (String spoken : matches) {
            if (TextNormalizer.matches(spoken, expectedPhrase)) {
                if (listener != null) {
                    listener.onSpeechResult(spoken, true);
                }
                return;
            }
        }
        
        // No match found - return best result
        String bestResult = matches.get(0);
        if (listener != null) {
            listener.onSpeechResult(bestResult, false);
        }
    }
    
    private void handleError(int error) {
        String message;
        boolean canRetry = true;
        
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client error";
                canRetry = false;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Microphone permission needed";
                canRetry = false;
                break;
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network error - offline mode not available";
                canRetry = false;
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "I didn't understand. Try again!";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Please wait...";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error";
                canRetry = true;
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "I didn't hear you. Speak louder!";
                break;
            default:
                message = "Speech error";
        }
        
        if (listener != null) {
            listener.onError(message, canRetry);
        }
    }
    
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Release resources. Call in Activity's onDestroy.
     */
    public void destroy() {
        stopListening();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
    
    /**
     * Call in Activity's onPause to stop listening.
     */
    public void onPause() {
        stopListening();
    }
}
