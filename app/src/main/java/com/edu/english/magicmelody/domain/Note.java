package com.edu.english.magicmelody.domain;

import java.util.List;
import java.util.ArrayList;

/**
 * 🎵 Note - Represents a rhythm game note
 */
public class Note {
    
    private long id;
    private int lane;           // 0-3 for 4 lanes
    private long spawnTime;     // When note appears
    private long hitTime;       // When note should be hit
    private NoteType type;
    private String word;        // Optional word for word notes
    private boolean isHit;
    private boolean isMissed;
    
    public enum NoteType {
        NORMAL,
        HOLD,
        SWIPE,
        WORD
    }
    
    public Note() {
        this.type = NoteType.NORMAL;
    }
    
    public Note(int lane, long spawnTime, long hitTime) {
        this.lane = lane;
        this.spawnTime = spawnTime;
        this.hitTime = hitTime;
        this.type = NoteType.NORMAL;
    }
    
    public Note(int lane, long spawnTime, long hitTime, String word) {
        this(lane, spawnTime, hitTime);
        this.word = word;
        this.type = NoteType.WORD;
    }
    
    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public int getLane() { return lane; }
    public void setLane(int lane) { this.lane = lane; }
    
    public long getSpawnTime() { return spawnTime; }
    public void setSpawnTime(long spawnTime) { this.spawnTime = spawnTime; }
    
    public long getHitTime() { return hitTime; }
    public void setHitTime(long hitTime) { this.hitTime = hitTime; }
    
    public NoteType getType() { return type; }
    public void setType(NoteType type) { this.type = type; }
    
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    
    public boolean hasWord() { return word != null && !word.isEmpty(); }
    
    public boolean isHit() { return isHit; }
    public void setHit(boolean hit) { isHit = hit; }
    
    public boolean isMissed() { return isMissed; }
    public void setMissed(boolean missed) { isMissed = missed; }
}
