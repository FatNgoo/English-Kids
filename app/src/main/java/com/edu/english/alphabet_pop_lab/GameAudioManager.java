package com.edu.english.alphabet_pop_lab;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GameAudioManager - Handles all audio for Alphabet Pop Lab
 * Manages SFX (pop sounds) and TTS (letter/word pronunciation)
 */
public class GameAudioManager {
    
    private Context context;
    
    // SoundPool for short sound effects
    private SoundPool soundPool;
    private Map<String, Integer> soundIds;
    private boolean soundPoolReady = false;
    
    // TextToSpeech for letter and word pronunciation
    private TextToSpeech tts;
    private boolean ttsReady = false;
    
    // Volume settings
    private float sfxVolume = 1.0f;
    private float voiceVolume = 1.0f;
    private boolean isMuted = false;
    
    // Sound effect IDs
    public static final String SOUND_POP = "pop";
    public static final String SOUND_WHOOSH = "whoosh";
    public static final String SOUND_SUCCESS = "success";
    public static final String SOUND_TAP = "tap";
    
    public GameAudioManager(Context context) {
        this.context = context;
        this.soundIds = new HashMap<>();
        
        initSoundPool();
        initTextToSpeech();
    }
    
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build();
        
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundPoolReady = true;
            }
        });
        
        // Note: In a real app, you would load actual sound files here
        // For now, we'll generate simple sounds programmatically
        // loadSound(SOUND_POP, R.raw.pop);
        // loadSound(SOUND_WHOOSH, R.raw.whoosh);
        
        soundPoolReady = true;
    }
    
    private void initTextToSpeech() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_MISSING_DATA && 
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsReady = true;
                    
                    // Set speech rate slightly slower for kids
                    tts.setSpeechRate(0.85f);
                    tts.setPitch(1.1f);
                }
            }
        });
    }
    
    /**
     * Load a sound effect from resources
     */
    public void loadSound(String name, int resourceId) {
        int soundId = soundPool.load(context, resourceId, 1);
        soundIds.put(name, soundId);
    }
    
    /**
     * Play a sound effect
     */
    public void playSound(String soundName) {
        if (isMuted || !soundPoolReady) return;
        
        Integer soundId = soundIds.get(soundName);
        if (soundId != null) {
            soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f);
        }
    }
    
    /**
     * Play pop sound effect
     */
    public void playPopSound() {
        // Since we don't have actual sound files, we'll use TTS for feedback
        // In production, replace with actual sound effect
        playSound(SOUND_POP);
    }
    
    /**
     * Speak a single letter clearly
     */
    public void speakLetter(char letter) {
        if (isMuted || !ttsReady) return;
        
        // Speak the letter name clearly
        String letterText = String.valueOf(letter);
        tts.speak(letterText, TextToSpeech.QUEUE_FLUSH, null, "letter_" + letter);
    }
    
    /**
     * Speak a word
     */
    public void speakWord(String word) {
        if (isMuted || !ttsReady) return;
        
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_" + word);
    }
    
    /**
     * Speak letter followed by word
     * Example: "A... Apple!"
     */
    public void speakLetterAndWord(char letter, String word) {
        if (isMuted || !ttsReady) return;
        
        // First speak the letter
        String text = letter + "... " + word + "!";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "letter_word");
    }
    
    /**
     * Speak the phonetic sound of a letter
     */
    public void speakPhonetic(char letter) {
        if (isMuted || !ttsReady) return;
        
        String phonetic = getPhoneticSound(letter);
        tts.speak(phonetic, TextToSpeech.QUEUE_ADD, null, "phonetic_" + letter);
    }
    
    /**
     * Get phonetic sound for a letter
     */
    private String getPhoneticSound(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A': return "ah";
            case 'B': return "buh";
            case 'C': return "kuh";
            case 'D': return "duh";
            case 'E': return "eh";
            case 'F': return "fff";
            case 'G': return "guh";
            case 'H': return "huh";
            case 'I': return "ih";
            case 'J': return "juh";
            case 'K': return "kuh";
            case 'L': return "lll";
            case 'M': return "mmm";
            case 'N': return "nnn";
            case 'O': return "oh";
            case 'P': return "puh";
            case 'Q': return "kwuh";
            case 'R': return "rrr";
            case 'S': return "sss";
            case 'T': return "tuh";
            case 'U': return "uh";
            case 'V': return "vvv";
            case 'W': return "wuh";
            case 'X': return "ks";
            case 'Y': return "yuh";
            case 'Z': return "zzz";
            default: return String.valueOf(letter);
        }
    }
    
    /**
     * Stop any current speech
     */
    public void stopSpeaking() {
        if (tts != null) {
            tts.stop();
        }
    }
    
    /**
     * Set SFX volume (0.0 to 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    /**
     * Set voice volume (0.0 to 1.0)
     */
    public void setVoiceVolume(float volume) {
        this.voiceVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    /**
     * Toggle mute
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            stopSpeaking();
        }
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Release all resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    
    /**
     * Check if TTS is ready
     */
    public boolean isTtsReady() {
        return ttsReady;
    }
}
