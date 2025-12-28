package com.edu.english.magicmelody.gameplay.boss;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 🎤 Voice Recognition Manager
 * 
 * Handles voice input for boss battle attacks:
 * - Speech-to-text recognition
 * - Word matching and scoring
 * - Pronunciation feedback
 */
public class VoiceRecognitionManager {
    
    private static final String TAG = "VoiceRecognitionMgr";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public enum RecognitionState {
        IDLE,
        LISTENING,
        PROCESSING,
        RESULT,
        ERROR
    }
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private RecognitionState currentState = RecognitionState.IDLE;
    
    // Target word for matching
    private String targetWord;
    private List<String> acceptableVariations = new ArrayList<>();
    
    // Configuration
    private Locale recognitionLocale = Locale.US;
    private int maxAlternatives = 5;
    private float minimumConfidence = 0.5f;
    
    // Timing
    private long listeningStartTime;
    private long maxListeningDurationMs = 5000;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface VoiceRecognitionListener {
        void onListeningStarted();
        void onListeningStopped();
        void onPartialResult(String partialText);
        void onRecognitionResult(RecognitionResult result);
        void onVolumeChanged(float volume);
        void onError(VoiceError error);
    }
    
    public enum VoiceError {
        NO_MICROPHONE,
        NO_PERMISSION,
        NETWORK_ERROR,
        NO_SPEECH_DETECTED,
        RECOGNITION_FAILED,
        BUSY
    }
    
    public static class RecognitionResult {
        public final String recognizedText;
        public final String targetWord;
        public final boolean isMatch;
        public final float confidence;
        public final float pronunciationScore;
        public final long responseTimeMs;
        public final List<String> alternatives;
        
        public RecognitionResult(String recognized, String target, boolean match,
                                float confidence, float pronScore, long time, 
                                List<String> alts) {
            this.recognizedText = recognized;
            this.targetWord = target;
            this.isMatch = match;
            this.confidence = confidence;
            this.pronunciationScore = pronScore;
            this.responseTimeMs = time;
            this.alternatives = alts;
        }
        
        public boolean isPerfect() {
            return isMatch && pronunciationScore >= 0.9f;
        }
        
        public boolean isGood() {
            return isMatch && pronunciationScore >= 0.7f;
        }
    }
    
    private VoiceRecognitionListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public VoiceRecognitionManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(VoiceRecognitionListener listener) {
        this.listener = listener;
    }
    
    public void setLocale(Locale locale) {
        this.recognitionLocale = locale;
    }
    
    public void setMinimumConfidence(float confidence) {
        this.minimumConfidence = confidence;
    }
    
    public void setMaxListeningDuration(long durationMs) {
        this.maxListeningDurationMs = durationMs;
    }
    
    /**
     * Check if speech recognition is available
     */
    public boolean isAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎤 RECOGNITION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize speech recognizer
     */
    public void initialize() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(createRecognitionListener());
    }
    
    /**
     * Start listening for a specific word
     */
    public void startListening(String targetWord) {
        startListening(targetWord, null);
    }
    
    /**
     * Start listening with acceptable variations
     */
    public void startListening(String targetWord, List<String> variations) {
        if (currentState == RecognitionState.LISTENING) {
            if (listener != null) {
                listener.onError(VoiceError.BUSY);
            }
            return;
        }
        
        if (!isAvailable()) {
            if (listener != null) {
                listener.onError(VoiceError.RECOGNITION_FAILED);
            }
            return;
        }
        
        this.targetWord = targetWord.toLowerCase().trim();
        this.acceptableVariations.clear();
        if (variations != null) {
            for (String v : variations) {
                acceptableVariations.add(v.toLowerCase().trim());
            }
        }
        
        // Create intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                       RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, recognitionLocale.toString());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxAlternatives);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        // Start
        currentState = RecognitionState.LISTENING;
        listeningStartTime = System.currentTimeMillis();
        
        if (speechRecognizer == null) {
            initialize();
        }
        
        try {
            speechRecognizer.startListening(intent);
            
            if (listener != null) {
                listener.onListeningStarted();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting recognition", e);
            currentState = RecognitionState.ERROR;
            if (listener != null) {
                listener.onError(VoiceError.RECOGNITION_FAILED);
            }
        }
    }
    
    /**
     * Stop listening
     */
    public void stopListening() {
        if (speechRecognizer != null && currentState == RecognitionState.LISTENING) {
            speechRecognizer.stopListening();
        }
        currentState = RecognitionState.IDLE;
        
        if (listener != null) {
            listener.onListeningStopped();
        }
    }
    
    /**
     * Cancel recognition
     */
    public void cancel() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
        currentState = RecognitionState.IDLE;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 RESULT PROCESSING
    // ═══════════════════════════════════════════════════════════════
    
    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }
            
            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech started");
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Volume level changed
                float normalized = Math.max(0, Math.min(1, (rmsdB + 2) / 12));
                if (listener != null) {
                    listener.onVolumeChanged(normalized);
                }
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Audio buffer received
            }
            
            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
                currentState = RecognitionState.PROCESSING;
            }
            
            @Override
            public void onError(int error) {
                currentState = RecognitionState.ERROR;
                VoiceError voiceError = mapError(error);
                
                if (listener != null) {
                    listener.onError(voiceError);
                    listener.onListeningStopped();
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                currentState = RecognitionState.RESULT;
                processResults(results);
                
                if (listener != null) {
                    listener.onListeningStopped();
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                
                if (matches != null && !matches.isEmpty() && listener != null) {
                    listener.onPartialResult(matches.get(0));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                // Additional events
            }
        };
    }
    
    private void processResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION);
        float[] confidences = results.getFloatArray(
            SpeechRecognizer.CONFIDENCE_SCORES);
        
        if (matches == null || matches.isEmpty()) {
            if (listener != null) {
                listener.onError(VoiceError.NO_SPEECH_DETECTED);
            }
            return;
        }
        
        // Find best match
        String bestMatch = null;
        float bestConfidence = 0;
        boolean isMatch = false;
        
        for (int i = 0; i < matches.size(); i++) {
            String match = matches.get(i).toLowerCase().trim();
            float confidence = (confidences != null && i < confidences.length) 
                              ? confidences[i] : 0.5f;
            
            // Check if matches target or variations
            if (match.equals(targetWord) || 
                match.contains(targetWord) ||
                targetWord.contains(match) ||
                acceptableVariations.contains(match)) {
                
                if (confidence > bestConfidence || !isMatch) {
                    bestMatch = matches.get(i);
                    bestConfidence = confidence;
                    isMatch = true;
                }
            } else if (!isMatch && confidence > bestConfidence) {
                bestMatch = matches.get(i);
                bestConfidence = confidence;
            }
        }
        
        if (bestMatch == null) {
            bestMatch = matches.get(0);
            bestConfidence = (confidences != null && confidences.length > 0) 
                            ? confidences[0] : 0.5f;
        }
        
        // Calculate pronunciation score
        float pronunciationScore = calculatePronunciationScore(
            bestMatch.toLowerCase(), targetWord, bestConfidence);
        
        // Calculate response time
        long responseTime = System.currentTimeMillis() - listeningStartTime;
        
        // Create result
        RecognitionResult result = new RecognitionResult(
            bestMatch,
            targetWord,
            isMatch,
            bestConfidence,
            pronunciationScore,
            responseTime,
            matches
        );
        
        if (listener != null) {
            listener.onRecognitionResult(result);
        }
        
        currentState = RecognitionState.IDLE;
    }
    
    /**
     * Calculate pronunciation score based on similarity
     */
    private float calculatePronunciationScore(String spoken, String target, float confidence) {
        if (spoken.equals(target)) {
            return confidence;
        }
        
        // Calculate Levenshtein distance ratio
        int distance = levenshteinDistance(spoken, target);
        int maxLength = Math.max(spoken.length(), target.length());
        float similarity = 1.0f - ((float) distance / maxLength);
        
        // Combine with confidence
        return (similarity * 0.7f) + (confidence * 0.3f);
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Map Android error codes to VoiceError
     */
    private VoiceError mapError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return VoiceError.NO_MICROPHONE;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return VoiceError.NO_PERMISSION;
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return VoiceError.NETWORK_ERROR;
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return VoiceError.NO_SPEECH_DETECTED;
            case SpeechRecognizer.ERROR_CLIENT:
                return VoiceError.BUSY;
            default:
                return VoiceError.RECOGNITION_FAILED;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public RecognitionState getCurrentState() {
        return currentState;
    }
    
    public boolean isListening() {
        return currentState == RecognitionState.LISTENING;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        listener = null;
        currentState = RecognitionState.IDLE;
    }
}
