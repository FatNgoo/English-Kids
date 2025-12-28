package com.edu.english.magicmelody;

import android.content.Context;

import com.edu.english.magicmelody.audio.AudioMixer;
import com.edu.english.magicmelody.audio.SoundPoolManager;
import com.edu.english.magicmelody.audio.MusicPlayer;
import com.edu.english.magicmelody.audio.NoteAudioPlayer;
import com.edu.english.magicmelody.audio.AudioEffectProcessor;
import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.gameplay.achievement.AchievementManager;
import com.edu.english.magicmelody.gameplay.notebook.MagicNotebookManager;
import com.edu.english.magicmelody.gameplay.world.EnvironmentEvolution;
import com.edu.english.magicmelody.gameplay.engine.RhythmEngine;
import com.edu.english.magicmelody.gameplay.engine.ScoreManager;
import com.edu.english.magicmelody.gameplay.engine.DifficultyManager;
import com.edu.english.magicmelody.gameplay.engine.GameStateManager;
import com.edu.english.magicmelody.gameplay.engine.WordCollector;

/**
 * 🔧 Service Locator
 * 
 * Central access point for all app services and managers.
 * Provides dependency resolution without full DI framework.
 */
public class ServiceLocator {
    
    private static ServiceLocator instance;
    private Context applicationContext;
    
    // Audio
    private AudioEffectProcessor audioEffectProcessor;
    private NoteAudioPlayer noteAudioPlayer;
    
    // Gameplay
    private RhythmEngine rhythmEngine;
    private ScoreManager scoreManager;
    private DifficultyManager difficultyManager;
    private GameStateManager gameStateManager;
    private WordCollector wordCollector;
    
    // Features
    private EnvironmentEvolution environmentEvolution;
    
    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }
    
    private ServiceLocator() {
    }
    
    public void initialize(Context context) {
        this.applicationContext = context.getApplicationContext();
    }
    
    public MagicMelodyDatabase getDatabase() {
        return MagicMelodyDatabase.getInstance(applicationContext);
    }
    
    public AudioMixer getAudioMixer() {
        return AudioMixer.getInstance();
    }
    
    public SoundPoolManager getSoundPoolManager() {
        return SoundPoolManager.getInstance();
    }
    
    public MusicPlayer getMusicPlayer() {
        return MusicPlayer.getInstance();
    }
    
    public synchronized NoteAudioPlayer getNoteAudioPlayer() {
        if (noteAudioPlayer == null) {
            noteAudioPlayer = new NoteAudioPlayer(applicationContext);
        }
        return noteAudioPlayer;
    }
    
    public synchronized AudioEffectProcessor getAudioEffectProcessor() {
        if (audioEffectProcessor == null) {
            audioEffectProcessor = new AudioEffectProcessor();
        }
        return audioEffectProcessor;
    }
    
    public synchronized RhythmEngine getRhythmEngine() {
        if (rhythmEngine == null) {
            rhythmEngine = new RhythmEngine();
        }
        return rhythmEngine;
    }
    
    public synchronized ScoreManager getScoreManager() {
        if (scoreManager == null) {
            scoreManager = new ScoreManager();
        }
        return scoreManager;
    }
    
    public synchronized DifficultyManager getDifficultyManager() {
        if (difficultyManager == null) {
            difficultyManager = new DifficultyManager();
        }
        return difficultyManager;
    }
    
    public synchronized GameStateManager getGameStateManager() {
        if (gameStateManager == null) {
            gameStateManager = new GameStateManager();
        }
        return gameStateManager;
    }
    
    public synchronized WordCollector getWordCollector() {
        if (wordCollector == null) {
            wordCollector = new WordCollector();
        }
        return wordCollector;
    }
    
    public AchievementManager getAchievementManager() {
        return AchievementManager.getInstance();
    }
    
    public MagicNotebookManager getMagicNotebookManager() {
        return MagicNotebookManager.getInstance();
    }
    
    public synchronized EnvironmentEvolution getEnvironmentEvolution() {
        if (environmentEvolution == null) {
            environmentEvolution = new EnvironmentEvolution();
        }
        return environmentEvolution;
    }
    
    public RhythmEngine createRhythmEngine() {
        return new RhythmEngine();
    }
    
    public ScoreManager createScoreManager() {
        return new ScoreManager();
    }
    
    public GameStateManager createGameStateManager() {
        return new GameStateManager();
    }
    
    public WordCollector createWordCollector() {
        return new WordCollector();
    }
    
    public void clearGameplayCache() {
        if (rhythmEngine != null) {
            rhythmEngine.stop();
            rhythmEngine = null;
        }
        scoreManager = null;
        gameStateManager = null;
        wordCollector = null;
    }
    
    public void release() {
        clearGameplayCache();
        
        if (noteAudioPlayer != null) {
            noteAudioPlayer.release();
            noteAudioPlayer = null;
        }
        
        if (audioEffectProcessor != null) {
            audioEffectProcessor.release();
            audioEffectProcessor = null;
        }
        
        environmentEvolution = null;
    }
}
