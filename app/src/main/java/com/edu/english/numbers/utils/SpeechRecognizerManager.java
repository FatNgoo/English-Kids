package com.edu.english.numbers.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manager for Speech Recognition in Numbers lesson
 * Handles voice input for number pronunciation verification
 * 
 * IMPORTANT: This must be used from the main thread (Activity context)
 */
public class SpeechRecognizerManager {
    
    private static final String TAG = "SpeechRecognizerManager";
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private OnSpeechResultListener listener;
    private boolean isListening = false;
    private boolean isAvailable = false;
    private Handler mainHandler;
    
    // Flag to track if we need to recreate the recognizer
    private boolean needsReinit = false;
    
    public interface OnSpeechResultListener {
        void onSpeechReady();
        void onSpeechStart();
        void onSpeechResult(String result);
        void onSpeechError(String error, boolean canRetry);
        void onSpeechEnd();
        void onSpeechNotAvailable();
        default void onRmsChanged(float rmsdB) {} // For visual feedback
    }
    
    /**
     * Create manager - MUST be called from Activity (not Application context)
     */
    public SpeechRecognizerManager(Context context, OnSpeechResultListener listener) {
        // Use Activity context directly, not application context
        // SpeechRecognizer requires Activity context on some devices
        this.context = context;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        checkAvailability();
    }
    
    /**
     * Check if speech recognition is available on this device
     */
    private void checkAvailability() {
        isAvailable = SpeechRecognizer.isRecognitionAvailable(context);
        
        if (!isAvailable) {
            Log.w(TAG, "Speech recognition not available on this device");
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
        } else {
            Log.d(TAG, "Speech recognition is available");
            // Don't init here - init when needed
        }
    }
    
    /**
     * Initialize the speech recognizer (must be on main thread)
     */
    private void initRecognizer() {
        // Ensure we're on main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::initRecognizer);
            return;
        }
        
        // Destroy old one if exists
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Error destroying old recognizer: " + e.getMessage());
            }
            speechRecognizer = null;
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(createRecognitionListener());
            needsReinit = false;
            Log.d(TAG, "Speech recognizer initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize speech recognizer: " + e.getMessage());
            isAvailable = false;
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
        }
    }
    
    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
                isListening = true;
                if (listener != null) {
                    listener.onSpeechReady();
                }
            }
            
            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech beginning - user is speaking");
                if (listener != null) {
                    listener.onSpeechStart();
                }
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Visual feedback of audio level
                if (listener != null) {
                    listener.onRmsChanged(rmsdB);
                }
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Raw audio buffer - not used
            }
            
            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
                isListening = false;
                if (listener != null) {
                    listener.onSpeechEnd();
                }
            }
            
            @Override
            public void onError(int error) {
                isListening = false;
                needsReinit = true; // Reinit after error
                
                String errorMessage = getErrorMessage(error);
                boolean canRetry = canRetryAfterError(error);
                Log.e(TAG, "Speech recognition error code " + error + ": " + errorMessage);
                
                if (listener != null) {
                    listener.onSpeechError(errorMessage, canRetry);
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                needsReinit = true; // Reinit after successful recognition
                
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                
                if (matches != null && !matches.isEmpty()) {
                    // Log all matches for debugging
                    Log.d(TAG, "Speech results (" + matches.size() + " matches):");
                    for (int i = 0; i < matches.size(); i++) {
                        Log.d(TAG, "  [" + i + "] " + matches.get(i));
                    }
                    
                    // Get the most likely result
                    String result = matches.get(0);
                    
                    if (listener != null) {
                        listener.onSpeechResult(result);
                    }
                } else {
                    Log.w(TAG, "No speech matches found in results");
                    if (listener != null) {
                        listener.onSpeechError("Couldn't understand. Please speak clearly.", true);
                    }
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty()) {
                    Log.d(TAG, "Partial result: " + partial.get(0));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "Speech event: " + eventType);
            }
        };
    }
    
    /**
     * Start listening for speech
     */
    public void startListening() {
        // Ensure on main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::startListening);
            return;
        }
        
        if (!isAvailable) {
            Log.w(TAG, "Speech recognition not available");
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
            return;
        }
        
        if (isListening) {
            Log.w(TAG, "Already listening - stopping first");
            stopListening();
        }
        
        // Reinitialize if needed (required after each recognition on some devices)
        if (speechRecognizer == null || needsReinit) {
            initRecognizer();
        }
        
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer is null after init");
            if (listener != null) {
                listener.onSpeechError("Failed to initialize speech recognition", false);
            }
            return;
        }
        
        try {
            Intent intent = createRecognizerIntent();
            speechRecognizer.startListening(intent);
            Log.d(TAG, "Started listening for speech...");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start listening: " + e.getMessage());
            e.printStackTrace();
            if (listener != null) {
                listener.onSpeechError("Failed to start voice recognition: " + e.getMessage(), true);
            }
        }
    }
    
    /**
     * Create the intent for speech recognition
     */
    private Intent createRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        
        // Language model - use FREE_FORM for better single word recognition
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        
        // Set language to US English
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        
        // Get multiple results to check alternatives
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        
        // Enable partial results for feedback
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        // Prompt (not shown but helps some recognizers)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the number");
        
        // Longer timeouts for kids who may need more time
        // Minimum speech length in ms
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L);
        
        // How long to wait after speech stops to consider it done
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);
        
        // Possible silence length
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L);
        
        return intent;
    }
    
    /**
     * Stop listening
     */
    public void stopListening() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::stopListening);
            return;
        }
        
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                isListening = false;
                Log.d(TAG, "Stopped listening");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Cancel current recognition
     */
    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::cancel);
            return;
        }
        
        if (speechRecognizer != null) {
            try {
                speechRecognizer.cancel();
                isListening = false;
                needsReinit = true;
                Log.d(TAG, "Recognition cancelled");
            } catch (Exception e) {
                Log.e(TAG, "Error cancelling: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if currently listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Check if speech recognition is available
     */
    public boolean isAvailable() {
        return isAvailable;
    }
    
    /**
     * Release resources - MUST call in onDestroy
     */
    public void destroy() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::destroy);
            return;
        }
        
        if (speechRecognizer != null) {
            try {
                speechRecognizer.cancel();
                speechRecognizer.destroy();
                speechRecognizer = null;
            } catch (Exception e) {
                Log.e(TAG, "Error destroying recognizer: " + e.getMessage());
            }
        }
        isListening = false;
        Log.d(TAG, "Speech recognizer destroyed");
    }
    
    /**
     * Convert error code to human-readable message
     */
    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error. Check microphone.";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error. Please try again.";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission required";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error. Check your internet connection.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout. Check your connection.";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "Didn't catch that. Speak more clearly.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer is busy. Wait a moment.";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error. Try again later.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech detected. Tap mic and speak louder.";
            default:
                return "Recognition error (" + error + "). Please try again.";
        }
    }
    
    /**
     * Check if we should allow retry after this error
     */
    private boolean canRetryAfterError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            case SpeechRecognizer.ERROR_SERVER:
            case SpeechRecognizer.ERROR_AUDIO:
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            case SpeechRecognizer.ERROR_CLIENT:
                return true;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Set a new listener
     */
    public void setListener(OnSpeechResultListener listener) {
        this.listener = listener;
    }
}
