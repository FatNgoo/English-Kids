package com.edu.english.shapes;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.edu.english.shapes.models.ShapeType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * ShapeTTSManager - Text-to-Speech manager using Android's built-in TTS
 * Speaks shape names and vocabulary words with US English locale
 */
public class ShapeTTSManager {
    
    private static final String TAG = "ShapeTTSManager";
    
    private static ShapeTTSManager instance;
    
    private Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private boolean isInitializing = false;
    private OnSpeechCompleteListener currentListener;
    
    // Queue for pending speech requests
    private Queue<SpeechRequest> pendingQueue = new LinkedList<>();
    
    private static class SpeechRequest {
        String text;
        OnSpeechCompleteListener listener;
        
        SpeechRequest(String text, OnSpeechCompleteListener listener) {
            this.text = text;
            this.listener = listener;
        }
    }
    
    public interface OnSpeechCompleteListener {
        void onSpeechComplete();
        void onSpeechError(String error);
    }
    
    public static synchronized ShapeTTSManager getInstance(Context context) {
        if (instance == null) {
            instance = new ShapeTTSManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private ShapeTTSManager(Context context) {
        this.context = context;
        initTTS();
    }
    
    private void initTTS() {
        if (isInitializing) return;
        isInitializing = true;
        
        textToSpeech = new TextToSpeech(context, status -> {
            isInitializing = false;
            
            if (status == TextToSpeech.SUCCESS) {
                // Set US English locale
                int result = textToSpeech.setLanguage(Locale.US);
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "US English not supported, trying default");
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
                
                // Set speech rate - slightly slower for kids
                textToSpeech.setSpeechRate(0.85f);
                
                // Set pitch - slightly higher for friendlier sound
                textToSpeech.setPitch(1.1f);
                
                // Set utterance progress listener
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "Speech started: " + utteranceId);
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "Speech completed: " + utteranceId);
                        handleSpeechDone();
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "Speech error: " + utteranceId);
                        handleSpeechError("TTS Error");
                    }
                });
                
                isInitialized = true;
                Log.d(TAG, "TTS initialized successfully");
                
                // Process pending queue
                processPendingQueue();
                
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
                // Notify all pending listeners of error
                notifyAllPendingError("TTS initialization failed");
            }
        });
    }
    
    private void handleSpeechDone() {
        if (currentListener != null) {
            android.os.Handler handler = new android.os.Handler(context.getMainLooper());
            final OnSpeechCompleteListener listener = currentListener;
            currentListener = null;
            handler.post(() -> {
                listener.onSpeechComplete();
                // Process next in queue
                processPendingQueue();
            });
        } else {
            processPendingQueue();
        }
    }
    
    private void handleSpeechError(String error) {
        if (currentListener != null) {
            android.os.Handler handler = new android.os.Handler(context.getMainLooper());
            final OnSpeechCompleteListener listener = currentListener;
            currentListener = null;
            handler.post(() -> {
                listener.onSpeechError(error);
                // Process next in queue
                processPendingQueue();
            });
        } else {
            processPendingQueue();
        }
    }
    
    private void processPendingQueue() {
        if (!pendingQueue.isEmpty() && isInitialized) {
            SpeechRequest request = pendingQueue.poll();
            if (request != null) {
                speakInternal(request.text, request.listener);
            }
        }
    }
    
    private void notifyAllPendingError(String error) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        while (!pendingQueue.isEmpty()) {
            SpeechRequest request = pendingQueue.poll();
            if (request != null && request.listener != null) {
                final OnSpeechCompleteListener listener = request.listener;
                handler.post(() -> listener.onSpeechError(error));
            }
        }
    }
    
    /**
     * Speak the shape name with enthusiasm
     * E.g., "Square!" or "This is a Circle!"
     */
    public void speakShapeName(ShapeType shapeType, OnSpeechCompleteListener listener) {
        String text = "This is a " + shapeType.getEnglishName() + "!";
        speak(text, listener);
    }
    
    /**
     * Speak introduction for a shape
     * E.g., "Hi! I'm a Square!"
     */
    public void speakShapeIntroduction(ShapeType shapeType, OnSpeechCompleteListener listener) {
        String text = "Hi! I'm a " + shapeType.getEnglishName() + "!";
        speak(text, listener);
    }
    
    /**
     * Speak a vocabulary word
     */
    public void speakWord(String word, OnSpeechCompleteListener listener) {
        speak(word, listener);
    }
    
    /**
     * Speak vocabulary item with its shape
     * E.g., "Window. Window is a Square."
     */
    public void speakVocabularyItem(String objectName, ShapeType shapeType, 
                                     OnSpeechCompleteListener listener) {
        String text = objectName + ". " + objectName + " is a " + shapeType.getEnglishName() + ".";
        speak(text, listener);
    }
    
    /**
     * Speak encouragement phrases
     */
    public void speakEncouragement(OnSpeechCompleteListener listener) {
        String[] phrases = {
            "Great job!",
            "Well done!",
            "Excellent!",
            "You're doing great!",
            "Wonderful!",
            "Fantastic!"
        };
        String phrase = phrases[(int) (Math.random() * phrases.length)];
        speak(phrase, listener);
    }
    
    /**
     * Speak tracing instructions
     */
    public void speakTracingInstruction(ShapeType shapeType, OnSpeechCompleteListener listener) {
        String text = "Trace the " + shapeType.getEnglishName() + " with your finger!";
        speak(text, listener);
    }
    
    /**
     * Speak completion message
     */
    public void speakTracingComplete(OnSpeechCompleteListener listener) {
        speak("Yay! You did it! Great tracing!", listener);
    }
    
    /**
     * Speak any text
     */
    public void speak(String text, OnSpeechCompleteListener listener) {
        if (!isInitialized) {
            // Queue the text to speak after initialization
            pendingQueue.add(new SpeechRequest(text, listener));
            Log.d(TAG, "TTS not ready, queuing: " + text);
            
            // Try to reinitialize if not already doing so
            if (!isInitializing && textToSpeech == null) {
                initTTS();
            }
            return;
        }
        
        // If currently speaking, queue it
        if (textToSpeech.isSpeaking() || currentListener != null) {
            pendingQueue.add(new SpeechRequest(text, listener));
            Log.d(TAG, "TTS busy, queuing: " + text);
            return;
        }
        
        speakInternal(text, listener);
    }
    
    private void speakInternal(String text, OnSpeechCompleteListener listener) {
        currentListener = listener;
        
        // Create params for utterance ID
        String utteranceId = "utterance_" + System.currentTimeMillis();
        
        // Use the newer speak method if available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.os.Bundle bundle = new android.os.Bundle();
            int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId);
            if (result != TextToSpeech.SUCCESS) {
                Log.e(TAG, "TTS speak failed with result: " + result);
                // Fallback - still notify listener after a delay
                if (listener != null) {
                    android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                    handler.postDelayed(() -> {
                        listener.onSpeechError("TTS speak failed");
                        currentListener = null;
                        processPendingQueue();
                    }, 1500);
                }
            }
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
        
        Log.d(TAG, "Speaking: " + text);
    }
    
    /**
     * Stop any ongoing speech and clear queue
     */
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        currentListener = null;
        pendingQueue.clear();
        Log.d(TAG, "TTS stopped and queue cleared");
    }
    
    /**
     * Stop current speech, clear queue, and speak new text immediately
     * Use this when transitioning between scenes to avoid overlapping speech
     */
    public void stopAndSpeak(String text, OnSpeechCompleteListener listener) {
        stop(); // Stop everything first
        
        if (!isInitialized) {
            pendingQueue.add(new SpeechRequest(text, listener));
            Log.d(TAG, "TTS not ready, queuing after stop: " + text);
            return;
        }
        
        speakInternal(text, listener);
    }
    
    /**
     * Check if currently speaking
     */
    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }
    
    /**
     * Release TTS resources
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        isInitialized = false;
        instance = null;
    }
    
    /**
     * Check if TTS is available on this device
     */
    public boolean isAvailable() {
        return isInitialized && textToSpeech != null;
    }
}
