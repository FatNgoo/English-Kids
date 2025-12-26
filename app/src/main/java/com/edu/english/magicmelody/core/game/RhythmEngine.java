package com.edu.english.magicmelody.core.game;

import com.edu.english.magicmelody.data.model.Lesson;
import com.edu.english.magicmelody.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * RhythmEngine - Core rhythm game logic
 * Handles note spawning, hit detection, and scoring
 */
public class RhythmEngine {
    
    private static final long DEFAULT_APPROACH_TIME = 2000; // 2 seconds to fall
    
    private final NotePool notePool;
    private Lesson currentLesson;
    private Lesson.AgeConfig ageConfig;
    
    // Timing
    private long approachTimeMs;
    private int perfectWindowMs;
    private int goodWindowMs;
    
    // State
    private int currentNoteIndex;
    private long songStartTime;
    private boolean isPlaying;
    
    // Scoring
    private int score;
    private int combo;
    private int maxCombo;
    private int perfectCount;
    private int goodCount;
    private int missCount;
    private int totalNotes;
    
    // Progress
    private float progress; // 0.0 to 1.0
    
    // Glitch notes
    private int glitchNoteInterval;
    private int notesSinceLastGlitch;
    private List<String> glitchVocabs;
    
    // Callbacks
    public interface RhythmEngineListener {
        void onNoteSpawned(Note note);
        void onNoteHit(JudgeResult result);
        void onNoteMissed(Note note);
        void onComboChanged(int combo);
        void onProgressChanged(float progress);
        void onSongComplete(int score, int stars);
    }
    
    private RhythmEngineListener listener;
    
    public RhythmEngine() {
        this.notePool = new NotePool();
        this.approachTimeMs = DEFAULT_APPROACH_TIME;
        this.glitchVocabs = new ArrayList<>();
        reset();
    }
    
    /**
     * Initialize with lesson data and user profile
     */
    public void init(Lesson lesson, UserProfile profile) {
        reset();
        
        this.currentLesson = lesson;
        this.ageConfig = lesson.getConfigForAge(profile.getAgeGroup().name());
        
        if (ageConfig != null) {
            this.perfectWindowMs = ageConfig.getPerfectWindowMs();
            this.goodWindowMs = ageConfig.getGoodWindowMs();
            this.glitchNoteInterval = ageConfig.getGlitchNoteInterval();
            
            // Adjust approach time based on BPM
            // Faster BPM = shorter approach time
            int bpm = ageConfig.getBpm();
            this.approachTimeMs = (long) (60000.0 / bpm * 2); // 2 beats approach
        } else {
            // Default values
            this.perfectWindowMs = profile.getPerfectWindow();
            this.goodWindowMs = profile.getGoodWindow();
            this.glitchNoteInterval = profile.getGlitchNoteInterval();
        }
        
        if (lesson.getNoteChart() != null) {
            this.totalNotes = lesson.getNoteChart().size();
        }
    }
    
    /**
     * Reset engine state
     */
    public void reset() {
        notePool.reset();
        currentNoteIndex = 0;
        songStartTime = 0;
        isPlaying = false;
        score = 0;
        combo = 0;
        maxCombo = 0;
        perfectCount = 0;
        goodCount = 0;
        missCount = 0;
        progress = 0;
        notesSinceLastGlitch = 0;
    }
    
    /**
     * Start playing
     */
    public void start() {
        songStartTime = System.currentTimeMillis();
        isPlaying = true;
    }
    
    /**
     * Update engine state - called every frame
     * @param gameTimeMs Current game time
     */
    public void update(long gameTimeMs) {
        if (!isPlaying || currentLesson == null) return;
        
        List<Lesson.NoteEvent> noteChart = currentLesson.getNoteChart();
        if (noteChart == null || noteChart.isEmpty()) return;
        
        // Spawn upcoming notes
        while (currentNoteIndex < noteChart.size()) {
            Lesson.NoteEvent event = noteChart.get(currentNoteIndex);
            long spawnTime = event.getTimeMs() - approachTimeMs;
            
            if (gameTimeMs >= spawnTime) {
                spawnNote(event);
                currentNoteIndex++;
            } else {
                break;
            }
        }
        
        // Update active notes and check for misses
        for (Note note : notePool.getAllNotes()) {
            if (note.isInUse()) {
                boolean stillActive = note.update(gameTimeMs, approachTimeMs);
                
                if (note.isMissed() && stillActive) {
                    handleMiss(note);
                }
            }
        }
        
        // Update progress
        updateProgress();
    }
    
    /**
     * Spawn a note from a note event
     */
    private void spawnNote(Lesson.NoteEvent event) {
        Note note = notePool.obtain(event.getLane(), event.getTimeMs(), event.getVocabKey());
        
        if (note != null && listener != null) {
            listener.onNoteSpawned(note);
        }
    }
    
    /**
     * Handle key press - judge hit
     * @param lane Lane that was pressed (0-6)
     * @param pressTimeMs Time of key press
     * @return JudgeResult
     */
    public JudgeResult judgeKeyPress(int lane, long pressTimeMs) {
        Note closestNote = null;
        long closestDiff = Long.MAX_VALUE;
        
        // Find the closest note in this lane within the good window
        for (Note note : notePool.getAllNotes()) {
            if (note.isInUse() && note.isActive() && note.getLane() == lane) {
                long diff = Math.abs(pressTimeMs - note.getTargetTimeMs());
                
                if (diff <= goodWindowMs && diff < closestDiff) {
                    closestNote = note;
                    closestDiff = diff;
                }
            }
        }
        
        if (closestNote == null) {
            // No note to hit
            return new JudgeResult();
        }
        
        // Calculate timing difference
        long timingDiff = pressTimeMs - closestNote.getTargetTimeMs();
        
        // Determine judgment
        JudgeResult.Judgment judgment;
        if (Math.abs(timingDiff) <= perfectWindowMs) {
            judgment = JudgeResult.Judgment.PERFECT;
            perfectCount++;
        } else {
            judgment = JudgeResult.Judgment.GOOD;
            goodCount++;
        }
        
        // Create result
        JudgeResult result = new JudgeResult(
            judgment, lane, timingDiff, closestNote.getVocabKey()
        );
        
        // Update note state
        closestNote.setState(Note.STATE_HIT);
        
        // Update score and combo
        combo++;
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        
        int noteScore = result.getScore();
        if (combo > 10) {
            noteScore = (int) (noteScore * 1.5f);
        } else if (combo > 5) {
            noteScore = (int) (noteScore * 1.2f);
        }
        score += noteScore;
        
        // Notify listener
        if (listener != null) {
            listener.onNoteHit(result);
            listener.onComboChanged(combo);
        }
        
        updateProgress();
        
        return result;
    }
    
    /**
     * Handle missed note
     */
    private void handleMiss(Note note) {
        missCount++;
        combo = 0;
        
        if (listener != null) {
            listener.onNoteMissed(note);
            listener.onComboChanged(0);
        }
    }
    
    /**
     * Update progress (0.0 to 1.0)
     */
    private void updateProgress() {
        if (totalNotes > 0) {
            int hitNotes = perfectCount + goodCount + missCount;
            float newProgress = (float) hitNotes / totalNotes;
            
            if (newProgress != progress) {
                progress = newProgress;
                
                if (listener != null) {
                    listener.onProgressChanged(progress);
                }
                
                // Check for song completion
                if (hitNotes >= totalNotes) {
                    completeSong();
                }
            }
        }
    }
    
    /**
     * Complete the song
     */
    private void completeSong() {
        isPlaying = false;
        
        int stars = calculateStars();
        
        if (listener != null) {
            listener.onSongComplete(score, stars);
        }
    }
    
    /**
     * Calculate stars based on performance
     * @return 0-3 stars
     */
    public int calculateStars() {
        if (totalNotes == 0) return 0;
        
        float accuracy = (float) (perfectCount + goodCount) / totalNotes;
        float perfectRatio = (float) perfectCount / totalNotes;
        
        if (accuracy >= 0.95f && perfectRatio >= 0.8f) {
            return 3; // Excellent
        } else if (accuracy >= 0.8f) {
            return 2; // Good
        } else if (accuracy >= 0.6f) {
            return 1; // Pass
        }
        return 0; // Fail
    }
    
    /**
     * Get accuracy percentage
     */
    public float getAccuracy() {
        int total = perfectCount + goodCount + missCount;
        if (total == 0) return 100f;
        return ((float) (perfectCount + goodCount) / total) * 100f;
    }
    
    /**
     * Set glitch vocabs for intelligent review
     */
    public void setGlitchVocabs(List<String> vocabs) {
        this.glitchVocabs = vocabs != null ? vocabs : new ArrayList<>();
    }
    
    // Getters
    public boolean isPlaying() { return isPlaying; }
    public int getScore() { return score; }
    public int getCombo() { return combo; }
    public int getMaxCombo() { return maxCombo; }
    public int getPerfectCount() { return perfectCount; }
    public int getGoodCount() { return goodCount; }
    public int getMissCount() { return missCount; }
    public float getProgress() { return progress; }
    public int getTotalNotes() { return totalNotes; }
    public long getApproachTimeMs() { return approachTimeMs; }
    public NotePool getNotePool() { return notePool; }
    
    public void setListener(RhythmEngineListener listener) {
        this.listener = listener;
    }
}
