package com.edu.english.magicmelody.gameplay.engine;

import com.edu.english.magicmelody.model.HitResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 🎯 Hit Detector
 * 
 * Handles hit detection for rhythm gameplay:
 * - Timing window management
 * - Accuracy calculation
 * - Miss detection
 */
public class HitDetector {
    
    private static final String TAG = "HitDetector";
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ TIMING WINDOWS (in milliseconds)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Timing window configuration
     * Defines how forgiving each accuracy level is
     */
    public static class TimingWindows {
        public float perfectMs = 50f;   // ±50ms for PERFECT
        public float goodMs = 100f;     // ±100ms for GOOD
        public float okMs = 150f;       // ±150ms for OK
        public float missThresholdMs = 200f; // Beyond this is MISS
        
        // For kids mode - more forgiving
        public static TimingWindows forKids() {
            TimingWindows tw = new TimingWindows();
            tw.perfectMs = 80f;
            tw.goodMs = 150f;
            tw.okMs = 220f;
            tw.missThresholdMs = 300f;
            return tw;
        }
        
        // Normal mode
        public static TimingWindows normal() {
            return new TimingWindows();
        }
        
        // Hard mode - less forgiving
        public static TimingWindows hard() {
            TimingWindows tw = new TimingWindows();
            tw.perfectMs = 30f;
            tw.goodMs = 60f;
            tw.okMs = 100f;
            tw.missThresholdMs = 150f;
            return tw;
        }
    }
    
    private TimingWindows timingWindows = TimingWindows.forKids();
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 ACTIVE NOTES
    // ═══════════════════════════════════════════════════════════════
    
    private final List<NoteSpawner.SpawnedNote> activeNotes = new ArrayList<>();
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface HitDetectorListener {
        void onNoteHit(NoteSpawner.SpawnedNote note, HitResult result);
        void onNoteMiss(NoteSpawner.SpawnedNote note);
    }
    
    private HitDetectorListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public HitDetector() {
    }
    
    public HitDetector(TimingWindows windows) {
        this.timingWindows = windows;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setTimingWindows(TimingWindows windows) {
        this.timingWindows = windows;
    }
    
    public void setListener(HitDetectorListener listener) {
        this.listener = listener;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 NOTE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Add a note to be tracked for hit detection
     */
    public void addNote(NoteSpawner.SpawnedNote note) {
        activeNotes.add(note);
    }
    
    /**
     * Remove a note from tracking
     */
    public void removeNote(NoteSpawner.SpawnedNote note) {
        activeNotes.remove(note);
    }
    
    /**
     * Clear all active notes
     */
    public void clearNotes() {
        activeNotes.clear();
    }
    
    /**
     * Get count of active notes
     */
    public int getActiveNoteCount() {
        return activeNotes.size();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 HIT DETECTION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check for hit when player taps a lane
     * @param laneIndex The lane that was tapped
     * @param currentTimeMs Current song time in milliseconds
     * @return HitResult if a note was hit, null if no note in range
     */
    public HitResult checkHit(int laneIndex, float currentTimeMs) {
        NoteSpawner.SpawnedNote closestNote = null;
        float closestTiming = Float.MAX_VALUE;
        
        // Find the closest active note in this lane within hit window
        for (NoteSpawner.SpawnedNote note : activeNotes) {
            if (!note.isActive()) continue;
            if (note.getLaneIndex() != laneIndex) continue;
            
            float timingOffset = Math.abs(note.getTimingOffset(currentTimeMs));
            
            // Check if within max hit window
            if (timingOffset <= timingWindows.missThresholdMs) {
                if (timingOffset < closestTiming) {
                    closestTiming = timingOffset;
                    closestNote = note;
                }
            }
        }
        
        if (closestNote == null) {
            // No note to hit - this is an empty tap
            return null;
        }
        
        // Calculate hit result
        float actualOffset = closestNote.getTimingOffset(currentTimeMs);
        HitResult result = calculateHitResult(actualOffset);
        
        // Mark note as hit
        closestNote.markAsHit();
        activeNotes.remove(closestNote);
        
        // Notify listener
        if (listener != null) {
            listener.onNoteHit(closestNote, result);
        }
        
        return result;
    }
    
    /**
     * Calculate hit result based on timing offset
     */
    private HitResult calculateHitResult(float timingOffsetMs) {
        float absOffset = Math.abs(timingOffsetMs);
        
        HitResult.HitType hitType;
        
        if (absOffset <= timingWindows.perfectMs) {
            hitType = HitResult.HitType.PERFECT;
        } else if (absOffset <= timingWindows.goodMs) {
            hitType = HitResult.HitType.GOOD;
        } else if (absOffset <= timingWindows.okMs) {
            hitType = HitResult.HitType.OK;
        } else {
            hitType = HitResult.HitType.MISS;
        }
        
        return HitResult.fromTiming(timingOffsetMs, hitType);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ MISS DETECTION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check for missed notes
     * Call this every frame
     * @param currentTimeMs Current song time
     */
    public void checkMisses(float currentTimeMs) {
        Iterator<NoteSpawner.SpawnedNote> iterator = activeNotes.iterator();
        
        while (iterator.hasNext()) {
            NoteSpawner.SpawnedNote note = iterator.next();
            
            if (!note.isActive()) {
                iterator.remove();
                continue;
            }
            
            // Check if note has passed the hit window
            float timingOffset = note.getTimingOffset(currentTimeMs);
            
            if (timingOffset > timingWindows.missThresholdMs) {
                // Note is too late - mark as missed
                note.markAsMissed();
                iterator.remove();
                
                if (listener != null) {
                    listener.onNoteMiss(note);
                }
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ACCURACY HELPERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get accuracy percentage for a timing offset
     * 100% = perfect, decreasing as timing worsens
     */
    public float getAccuracyPercent(float timingOffsetMs) {
        float absOffset = Math.abs(timingOffsetMs);
        
        if (absOffset <= timingWindows.perfectMs) {
            return 100f;
        } else if (absOffset <= timingWindows.goodMs) {
            // Linear interpolation between 100% and 75%
            float ratio = (absOffset - timingWindows.perfectMs) / 
                         (timingWindows.goodMs - timingWindows.perfectMs);
            return 100f - (25f * ratio);
        } else if (absOffset <= timingWindows.okMs) {
            // Linear interpolation between 75% and 50%
            float ratio = (absOffset - timingWindows.goodMs) / 
                         (timingWindows.okMs - timingWindows.goodMs);
            return 75f - (25f * ratio);
        } else {
            // Beyond OK window - rapidly decrease to 0
            float ratio = (absOffset - timingWindows.okMs) / 
                         (timingWindows.missThresholdMs - timingWindows.okMs);
            return Math.max(0f, 50f - (50f * ratio));
        }
    }
    
    /**
     * Check if a timing would result in a successful hit (not miss)
     */
    public boolean isSuccessfulHit(float timingOffsetMs) {
        return Math.abs(timingOffsetMs) <= timingWindows.okMs;
    }
    
    /**
     * Get the timing window configuration
     */
    public TimingWindows getTimingWindows() {
        return timingWindows;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 RESET
    // ═══════════════════════════════════════════════════════════════
    
    public void reset() {
        activeNotes.clear();
    }
}
