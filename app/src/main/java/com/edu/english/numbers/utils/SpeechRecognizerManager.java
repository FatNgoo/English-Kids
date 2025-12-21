package com.edu.english.numbers.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manager for Speech Recognition in Numbers lesson
 * Handles voice input for number pronunciation verification
 */
public class SpeechRecognizerManager {
    
    private static final String TAG = "SpeechRecognizerManager";
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private OnSpeechResultListener listener;
    private boolean isListening = false;
    private boolean isAvailable = false;
    
    public interface OnSpeechResultListener {
        void onSpeechReady();
        void onSpeechStart();
        void onSpeechResult(String result);
        void onSpeechError(String error, boolean canRetry);
        void onSpeechEnd();
        void onSpeechNotAvailable();
    }
    
    public SpeechRecognizerManager(Context context, OnSpeechResultListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
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
            initRecognizer();
        }
    }
    
    /**
     * Initialize the speech recognizer
     */
    private void initRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                
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
                    Log.d(TAG, "Speech beginning");
                    if (listener != null) {
                        listener.onSpeechStart();
                    }
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {
                    // Can use for visual feedback of audio level
                }
                
                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Raw audio buffer
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
                    String errorMessage = getErrorMessage(error);
                    boolean canRetry = canRetryAfterError(error);
                    Log.e(TAG, "Speech recognition error: " + errorMessage);
                    
                    if (listener != null) {
                        listener.onSpeechError(errorMessage, canRetry);
                    }
                }
                
                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    
                    if (matches != null && !matches.isEmpty()) {
                        // Get the most likely result
                        String result = matches.get(0);
                        Log.d(TAG, "Speech result: " + result);
                        
                        if (listener != null) {
                            listener.onSpeechResult(result);
                        }
                    } else {
                        Log.w(TAG, "No speech matches found");
                        if (listener != null) {
                            listener.onSpeechError("Couldn't understand. Please try again.", true);
                        }
                    }
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Partial results while speaking
                }
                
                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Reserved for future events
                }
            });
            
            Log.d(TAG, "Speech recognizer initialized");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize speech recognizer: " + e.getMessage());
            isAvailable = false;
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
        }
    }
    
    /**
     * Start listening for speech
     */
    public void startListening() {
        if (!isAvailable) {
            Log.w(TAG, "Speech recognition not available");
            if (listener != null) {
                listener.onSpeechNotAvailable();
            }
            return;
        }
        
        if (isListening) {
            Log.w(TAG, "Already listening");
            return;
        }
        
        if (speechRecognizer == null) {
            initRecognizer();
        }
        
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.US.toString());
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            
            // Short phrases expected
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
            
            speechRecognizer.startListening(intent);
            Log.d(TAG, "Started listening");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start listening: " + e.getMessage());
            if (listener != null) {
                listener.onSpeechError("Failed to start voice recognition", true);
            }
        }
    }
    
    /**
     * Stop listening
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
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
        if (speechRecognizer != null) {
            try {
                speechRecognizer.cancel();
                isListening = false;
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
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission required";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error. Please check your connection.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "Didn't catch that. Please try again.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Speech recognizer is busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech detected. Tap the mic and speak.";
            default:
                return "Speech recognition error";
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
                return true;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
            case SpeechRecognizer.ERROR_CLIENT:
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
