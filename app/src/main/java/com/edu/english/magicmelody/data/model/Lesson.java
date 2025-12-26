package com.edu.english.magicmelody.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Model class for lesson data loaded from JSON
 * Contains all configuration for a rhythm gameplay session
 */
public class Lesson {
    
    @SerializedName("id")
    private int id;
    
    private String lessonId;
    private String title;
    private String titleVietnamese;
    private String baseSong; // Optional audio file
    private String theme; // NATURE, FANTASY, SCIFI
    private String mainCharacter;
    private AgeConfig toddler;
    private AgeConfig explorer;
    private AgeConfig master;
    
    @SerializedName("ageConfigs")
    private Map<String, AgeConfig> ageConfigs;
    
    @SerializedName("noteEvents")
    private List<NoteEvent> noteChart;
    
    @SerializedName("vocabItems")
    private List<VocabItem> vocabItems;
    
    private List<VocabItem> vocabulary;
    
    // Age-specific configuration
    public static class AgeConfig {
        private int bpm;
        private int notesVisible;
        
        @SerializedName("perfectWindow")
        private int perfectWindowMs;
        
        @SerializedName("goodWindow")
        private int goodWindowMs;
        
        private int glitchNoteInterval; // Inject glitch note every N notes
        
        public int getBpm() { return bpm; }
        public void setBpm(int bpm) { this.bpm = bpm; }
        
        public int getNotesVisible() { return notesVisible; }
        public void setNotesVisible(int notesVisible) { this.notesVisible = notesVisible; }
        
        public int getPerfectWindowMs() { return perfectWindowMs; }
        public void setPerfectWindowMs(int perfectWindowMs) { this.perfectWindowMs = perfectWindowMs; }
        
        public int getGoodWindowMs() { return goodWindowMs; }
        public void setGoodWindowMs(int goodWindowMs) { this.goodWindowMs = goodWindowMs; }
        
        public int getGlitchNoteInterval() { return glitchNoteInterval; }
        public void setGlitchNoteInterval(int glitchNoteInterval) { this.glitchNoteInterval = glitchNoteInterval; }
    }
    
    // Single note event in the chart
    public static class NoteEvent {
        private long timeMs; // Time in milliseconds when note should be hit
        private int beatIndex; // Alternative: beat number
        private int lane; // 0-6 for piano keys C-B
        private String vocabKey; // Reference to vocabulary item
        
        public long getTimeMs() { return timeMs; }
        public void setTimeMs(long timeMs) { this.timeMs = timeMs; }
        
        public int getBeatIndex() { return beatIndex; }
        public void setBeatIndex(int beatIndex) { this.beatIndex = beatIndex; }
        
        public int getLane() { return lane; }
        public void setLane(int lane) { this.lane = lane; }
        
        public String getVocabKey() { return vocabKey; }
        public void setVocabKey(String vocabKey) { this.vocabKey = vocabKey; }
    }
    
    // Vocabulary item
    public static class VocabItem {
        private String key;
        
        @SerializedName("word")
        private String english;
        
        @SerializedName("translation")
        private String vietnamese;
        
        private int lane;
        private int noteIndex; // Piano note (0=C, 1=D, etc)
        
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public String getEnglish() { return english; }
        public void setEnglish(String english) { this.english = english; }
        
        public String getVietnamese() { return vietnamese; }
        public void setVietnamese(String vietnamese) { this.vietnamese = vietnamese; }
        
        public int getLane() { return lane; }
        public void setLane(int lane) { this.lane = lane; }
        
        public int getNoteIndex() { return noteIndex; }
        public void setNoteIndex(int noteIndex) { this.noteIndex = noteIndex; }
    }
    
    // Getters and Setters
    public String getLessonId() { 
        // Return lessonId if set, otherwise generate from id
        if (lessonId != null) return lessonId;
        return "lesson_" + id;
    }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getTitleVietnamese() { return titleVietnamese; }
    public void setTitleVietnamese(String titleVietnamese) { this.titleVietnamese = titleVietnamese; }
    
    public String getBaseSong() { return baseSong; }
    public void setBaseSong(String baseSong) { this.baseSong = baseSong; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public String getMainCharacter() { return mainCharacter; }
    public void setMainCharacter(String mainCharacter) { this.mainCharacter = mainCharacter; }
    
    public AgeConfig getToddler() { return toddler; }
    public void setToddler(AgeConfig toddler) { this.toddler = toddler; }
    
    public AgeConfig getExplorer() { return explorer; }
    public void setExplorer(AgeConfig explorer) { this.explorer = explorer; }
    
    public AgeConfig getMaster() { return master; }
    public void setMaster(AgeConfig master) { this.master = master; }
    
    public List<NoteEvent> getNoteChart() { return noteChart; }
    public void setNoteChart(List<NoteEvent> noteChart) { this.noteChart = noteChart; }
    
    public List<VocabItem> getVocabulary() { 
        // Return vocabulary if set, otherwise return vocabItems
        return vocabulary != null ? vocabulary : vocabItems;
    }
    public void setVocabulary(List<VocabItem> vocabulary) { this.vocabulary = vocabulary; }
    
    public List<VocabItem> getVocabItems() { return vocabItems; }
    public void setVocabItems(List<VocabItem> vocabItems) { this.vocabItems = vocabItems; }
    
    /**
     * Get age-specific config based on age group string
     */
    public AgeConfig getConfigForAge(String ageGroup) {
        // First try ageConfigs map
        if (ageConfigs != null && ageConfigs.containsKey(ageGroup)) {
            return ageConfigs.get(ageGroup);
        }
        // Fallback to individual fields
        switch (ageGroup) {
            case "TODDLER": return toddler;
            case "EXPLORER": return explorer;
            case "MASTER": return master;
            default: return explorer; // Default to middle difficulty
        }
    }
}
