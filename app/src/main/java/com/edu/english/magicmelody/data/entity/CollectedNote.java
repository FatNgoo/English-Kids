package com.edu.english.magicmelody.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 🎵 CollectedNote Entity
 * 
 * Purpose: Track notes collected in Magic Notebook
 * 
 * Features:
 * - Each note collected from lessons goes into notebook
 * - Notes can be replayed for learning reinforcement
 * - Special/Legendary notes from boss battles
 */
@Entity(tableName = "collected_notes")
public class CollectedNote {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "note_id")
    private String noteId; // Unique identifier for this note type
    
    @ColumnInfo(name = "note_name")
    private String noteName; // "do", "re", "mi", etc.
    
    @ColumnInfo(name = "word")
    private String word; // Associated English word
    
    @ColumnInfo(name = "world_id")
    private String worldId; // Which world it came from
    
    @ColumnInfo(name = "lesson_id")
    private String lessonId; // Which lesson it was collected from
    
    // ═══════════════════════════════════════════════════════════════
    // 🏆 NOTE TYPE & RARITY
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Note type:
     * - "basic": Regular note from lessons
     * - "special": Bonus note from perfect scores
     * - "legendary": Note from boss battles
     */
    @ColumnInfo(name = "note_type")
    private String noteType;
    
    /**
     * Audio file path for this note
     */
    @ColumnInfo(name = "audio_file")
    private String audioFile;
    
    /**
     * Character associated with this note (for 3D pop-up)
     */
    @ColumnInfo(name = "character_id")
    private String characterId;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "times_played")
    private int timesPlayed;
    
    @ColumnInfo(name = "times_collected")
    private int timesCollected;
    
    @ColumnInfo(name = "perfect_hits")
    private int perfectHits;
    
    @ColumnInfo(name = "mastery_level")
    private int masteryLevel;
    
    @ColumnInfo(name = "collected_at")
    private long collectedAt;
    
    @ColumnInfo(name = "first_collected_at")
    private long firstCollectedAt;
    
    @ColumnInfo(name = "last_collected_at")
    private long lastCollectedAt;
    
    @ColumnInfo(name = "last_played_at")
    private long lastPlayedAt;
    
    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public CollectedNote() {
        // Default constructor for Room
    }
    
    /**
     * Create a new collected note
     */
    public static CollectedNote create(String userId, String noteName, String word, 
                                        String worldId, String lessonId, String noteType) {
        CollectedNote note = new CollectedNote();
        note.setUserId(userId);
        note.setNoteId(worldId + "_" + noteName + "_" + System.currentTimeMillis());
        note.setNoteName(noteName);
        note.setWord(word);
        note.setWorldId(worldId);
        note.setLessonId(lessonId);
        note.setNoteType(noteType);
        note.setAudioFile("magicmelody/audio/notes/" + noteName + ".mp3");
        note.setCollectedAt(System.currentTimeMillis());
        return note;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public int getTimesCollected() {
        return timesCollected;
    }

    public void setTimesCollected(int timesCollected) {
        this.timesCollected = timesCollected;
    }

    public int getPerfectHits() {
        return perfectHits;
    }

    public void setPerfectHits(int perfectHits) {
        this.perfectHits = perfectHits;
    }

    public int getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public long getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(long collectedAt) {
        this.collectedAt = collectedAt;
    }

    public long getFirstCollectedAt() {
        return firstCollectedAt;
    }

    public void setFirstCollectedAt(long firstCollectedAt) {
        this.firstCollectedAt = firstCollectedAt;
    }

    public long getLastCollectedAt() {
        return lastCollectedAt;
    }

    public void setLastCollectedAt(long lastCollectedAt) {
        this.lastCollectedAt = lastCollectedAt;
    }

    public long getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(long lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    /**
     * Alias for getTimesPlayed() - returns the play count
     */
    public int getPlayCount() {
        return timesPlayed;
    }
    
    /**
     * Check if this note is mastered (masteryLevel >= 100)
     */
    public boolean isMastered() {
        return masteryLevel >= 100;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Mark this note as played
     */
    public void play() {
        timesPlayed++;
        lastPlayedAt = System.currentTimeMillis();
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite() {
        isFavorite = !isFavorite;
    }
    
    /**
     * Check if this is a legendary note
     */
    public boolean isLegendary() {
        return "legendary".equals(noteType);
    }
    
    /**
     * Check if this is a special note
     */
    public boolean isSpecial() {
        return "special".equals(noteType);
    }
    
    /**
     * Get display emoji based on note type
     */
    public String getTypeEmoji() {
        switch (noteType) {
            case "legendary": return "👑";
            case "special": return "⭐";
            default: return "🎵";
        }
    }
}
