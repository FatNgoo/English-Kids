package com.edu.english.magicmelody.model;

import java.util.List;

/**
 * 🎵 LessonConfig Model
 * 
 * Purpose: Represents a lesson configuration loaded from JSON
 * This is NOT a Room entity - it's loaded from assets/magicmelody/lessons/
 * 
 * Used for:
 * - Defining note sequences
 * - BPM and difficulty settings
 * - Words to learn in each lesson
 */
public class LessonConfig {
    
    private String lessonId;
    private String ageGroup;
    private String worldId;
    private int level;
    private String title;
    private String description;
    private List<String> notes;
    private List<String> words;
    private int bpm;
    private int difficulty;
    private int starsRequired;
    private boolean isBossLevel;
    private List<NoteEvent> noteSequence;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public LessonConfig() {
        // Default constructor for JSON parsing
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getStarsRequired() {
        return starsRequired;
    }

    public void setStarsRequired(int starsRequired) {
        this.starsRequired = starsRequired;
    }

    public boolean isBossLevel() {
        return isBossLevel;
    }

    public void setBossLevel(boolean bossLevel) {
        isBossLevel = bossLevel;
    }

    public List<NoteEvent> getNoteSequence() {
        return noteSequence;
    }

    public void setNoteSequence(List<NoteEvent> noteSequence) {
        this.noteSequence = noteSequence;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get the duration of this lesson in seconds
     */
    public float getDurationSeconds() {
        if (noteSequence == null || noteSequence.isEmpty()) {
            return 0;
        }
        
        NoteEvent lastNote = noteSequence.get(noteSequence.size() - 1);
        return lastNote.getTime() + lastNote.getDuration() + 2f; // Add 2 seconds buffer
    }
    
    /**
     * Get total number of notes in this lesson
     */
    public int getTotalNotes() {
        return noteSequence != null ? noteSequence.size() : 0;
    }
    
    /**
     * Calculate max possible score for this lesson
     */
    public int getMaxPossibleScore() {
        // 100 points per perfect hit
        return getTotalNotes() * 100;
    }
    
    /**
     * Get difficulty display string
     */
    public String getDifficultyDisplay() {
        switch (difficulty) {
            case 1: return "⭐ Easy";
            case 2: return "⭐⭐ Normal";
            case 3: return "⭐⭐⭐ Hard";
            case 4: return "⭐⭐⭐⭐ Expert";
            case 5: return "⭐⭐⭐⭐⭐ Master";
            default: return "Unknown";
        }
    }
    
    /**
     * Get beat interval in milliseconds
     */
    public long getBeatIntervalMs() {
        return (long) (60000.0 / bpm);
    }
}
