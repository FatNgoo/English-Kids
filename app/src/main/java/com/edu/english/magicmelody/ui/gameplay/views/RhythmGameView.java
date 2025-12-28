package com.edu.english.magicmelody.ui.gameplay.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.edu.english.magicmelody.domain.Note;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 🎮 RhythmGameView - Custom View for rhythm gameplay
 * 
 * Features:
 * - 4 falling note lanes
 * - Visual hit feedback (Perfect/Good/OK/Miss)
 * - Combo counter display
 * - Word highlight for educational notes
 */
public class RhythmGameView extends View {
    
    // Lane configuration
    private static final int LANE_COUNT = 4;
    private static final float HIT_LINE_Y_RATIO = 0.85f;
    private static final float HIT_TOLERANCE = 150f; // pixels - larger for easier hits
    
    // Paints
    private Paint lanePaint;
    private Paint hitLinePaint;
    private Paint notePaint;
    private Paint wordPaint;
    private Paint feedbackPaint;
    
    // Game state
    private List<GameNote> activeNotes = new ArrayList<>();
    private float laneWidth;
    private float hitLineY;
    private int[] laneColors = {
        Color.parseColor("#FF6B6B"),  // Red
        Color.parseColor("#4ECDC4"),  // Teal
        Color.parseColor("#FFE66D"),  // Yellow
        Color.parseColor("#95E1D3")   // Mint
    };
    
    // Feedback display
    private String feedbackText = "";
    private long feedbackEndTime = 0;
    private int feedbackColor = Color.WHITE;
    
    // Listener
    private GameViewListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public RhythmGameView(Context context) {
        super(context);
        init();
    }
    
    public RhythmGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public RhythmGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Lane background paint
        lanePaint = new Paint();
        lanePaint.setStyle(Paint.Style.FILL);
        
        // Hit line paint
        hitLinePaint = new Paint();
        hitLinePaint.setColor(Color.WHITE);
        hitLinePaint.setStrokeWidth(8f);
        hitLinePaint.setStyle(Paint.Style.STROKE);
        
        // Note paint
        notePaint = new Paint();
        notePaint.setStyle(Paint.Style.FILL);
        notePaint.setAntiAlias(true);
        
        // Word paint
        wordPaint = new Paint();
        wordPaint.setColor(Color.WHITE);
        wordPaint.setTextSize(32f);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        wordPaint.setAntiAlias(true);
        
        // Feedback paint
        feedbackPaint = new Paint();
        feedbackPaint.setTextSize(72f);
        feedbackPaint.setTextAlign(Paint.Align.CENTER);
        feedbackPaint.setAntiAlias(true);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📐 SIZE CALCULATION
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        laneWidth = (float) w / LANE_COUNT;
        hitLineY = h * HIT_LINE_Y_RATIO;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        drawLanes(canvas);
        drawHitLine(canvas);
        drawNotes(canvas);
        drawFeedback(canvas);
    }
    
    private void drawLanes(Canvas canvas) {
        for (int i = 0; i < LANE_COUNT; i++) {
            float left = i * laneWidth;
            lanePaint.setColor(Color.argb(30, 
                Color.red(laneColors[i]), 
                Color.green(laneColors[i]), 
                Color.blue(laneColors[i])));
            canvas.drawRect(left, 0, left + laneWidth, getHeight(), lanePaint);
            
            // Lane divider
            lanePaint.setColor(Color.argb(50, 255, 255, 255));
            canvas.drawLine(left + laneWidth, 0, left + laneWidth, getHeight(), lanePaint);
        }
    }
    
    private void drawHitLine(Canvas canvas) {
        // Draw hit zone rectangle
        float hitZoneTop = hitLineY - HIT_TOLERANCE;
        float hitZoneBottom = hitLineY + HIT_TOLERANCE;
        
        lanePaint.setColor(Color.argb(30, 255, 255, 255));
        canvas.drawRect(0, hitZoneTop, getWidth(), hitZoneBottom, lanePaint);
        
        // Draw hit line
        canvas.drawLine(0, hitLineY, getWidth(), hitLineY, hitLinePaint);
    }
    
    private void drawNotes(Canvas canvas) {
        synchronized (activeNotes) {
            for (GameNote note : activeNotes) {
                float centerX = (note.lane + 0.5f) * laneWidth;
                float noteRadius = laneWidth * 0.35f;
                
                // Note color
                notePaint.setColor(laneColors[note.lane]);
                
                // Draw note
                canvas.drawCircle(centerX, note.currentY, noteRadius, notePaint);
                
                // Draw word if present
                if (note.word != null && !note.word.isEmpty()) {
                    canvas.drawText(note.word, centerX, note.currentY + 10, wordPaint);
                }
            }
        }
    }
    
    private void drawFeedback(Canvas canvas) {
        if (System.currentTimeMillis() < feedbackEndTime) {
            feedbackPaint.setColor(feedbackColor);
            canvas.drawText(feedbackText, getWidth() / 2f, getHeight() / 2f, feedbackPaint);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 👆 TOUCH INPUT
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int lane = (int) (event.getX() / laneWidth);
            handleLaneTap(lane);
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private void handleLaneTap(int lane) {
        if (lane < 0 || lane >= LANE_COUNT) return;
        
        synchronized (activeNotes) {
            GameNote hitNote = null;
            HitResult hitResult = null;
            float closestDistance = Float.MAX_VALUE;
            
            // Find the closest note in this lane that can be hit
            for (GameNote note : activeNotes) {
                if (note.lane == lane && !note.isHit) {
                    float distance = Math.abs(note.currentY - hitLineY);
                    
                    // Hit any note within generous range - prioritize closest
                    if (distance < HIT_TOLERANCE * 2.0f && distance < closestDistance) {
                        closestDistance = distance;
                        hitNote = note;
                        
                        // Determine hit quality
                        if (distance < HIT_TOLERANCE * 0.4f) {
                            hitResult = HitResult.PERFECT;
                        } else if (distance < HIT_TOLERANCE * 0.8f) {
                            hitResult = HitResult.GOOD;
                        } else {
                            hitResult = HitResult.OK;
                        }
                    }
                }
            }
            
            if (hitNote != null && hitResult != null) {
                hitNote.isHit = true;
                // Remove note immediately for instant feedback
                activeNotes.remove(hitNote);
                showFeedback(hitResult);
                if (listener != null) {
                    listener.onNoteHit(hitNote.originalNote, hitResult);
                }
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Public method to hit a specific lane (called from piano keys)
     */
    public void hitLane(int lane) {
        handleLaneTap(lane);
    }
    
    public void addNote(Note note) {
        synchronized (activeNotes) {
            GameNote gameNote = new GameNote();
            gameNote.originalNote = note;
            gameNote.lane = note.getLane();
            gameNote.word = note.getWord();
            gameNote.currentY = -50f; // Start above screen
            gameNote.targetTime = note.getHitTime();
            gameNote.isHit = false;
            activeNotes.add(gameNote);
        }
    }
    
    public void updateNotes(long currentTime, float noteSpeed) {
        synchronized (activeNotes) {
            Iterator<GameNote> iterator = activeNotes.iterator();
            while (iterator.hasNext()) {
                GameNote note = iterator.next();
                
                // Update Y position based on time
                float progress = (float)(currentTime - note.spawnTime) / 2000f;
                note.currentY = progress * (hitLineY + HIT_TOLERANCE * 2);
                
                // Remove notes that passed hit line without being hit
                if (note.currentY > getHeight()) {
                    if (!note.isHit) {
                        showFeedback(HitResult.MISS);
                        if (listener != null) {
                            listener.onNoteMiss(note.originalNote);
                        }
                    }
                    iterator.remove();
                }
            }
        }
        invalidate();
    }
    
    public void clearNotes() {
        synchronized (activeNotes) {
            activeNotes.clear();
        }
    }
    
    /**
     * Reset the game view to initial state
     */
    public void reset() {
        clearNotes();
        feedbackText = "";
        feedbackEndTime = 0;
        invalidate();
    }
    
    /**
     * Show hit feedback for a specific lane
     */
    public void showHitFeedback(int lane, String hitType) {
        HitResult result;
        switch (hitType.toUpperCase()) {
            case "PERFECT":
                result = HitResult.PERFECT;
                break;
            case "GOOD":
                result = HitResult.GOOD;
                break;
            case "OK":
                result = HitResult.OK;
                break;
            default:
                result = HitResult.MISS;
                break;
        }
        showFeedback(result);
    }
    
    /**
     * Show miss feedback for a specific lane
     */
    public void showMissFeedback(int lane) {
        showFeedback(HitResult.MISS);
    }
    
    /**
     * Remove a specific note from active notes
     */
    public void removeNote(Note note) {
        synchronized (activeNotes) {
            activeNotes.removeIf(gn -> gn.originalNote == note);
        }
        invalidate();
    }
    
    /**
     * Add a note with associated word (same as addNote, word is already in Note)
     */
    public void addWordNote(Note note, String word) {
        addNote(note);
    }
    
    public void showFeedback(HitResult result) {
        feedbackEndTime = System.currentTimeMillis() + 500;
        
        switch (result) {
            case PERFECT:
                feedbackText = "PERFECT!";
                feedbackColor = Color.parseColor("#FFD700");
                break;
            case GOOD:
                feedbackText = "GOOD!";
                feedbackColor = Color.parseColor("#4ECDC4");
                break;
            case OK:
                feedbackText = "OK";
                feedbackColor = Color.parseColor("#95E1D3");
                break;
            case MISS:
                feedbackText = "MISS";
                feedbackColor = Color.parseColor("#FF6B6B");
                break;
        }
        invalidate();
    }
    
    public void setGameViewListener(GameViewListener listener) {
        this.listener = listener;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 INNER CLASSES
    // ═══════════════════════════════════════════════════════════════
    
    public enum HitResult {
        PERFECT, GOOD, OK, MISS
    }
    
    private static class GameNote {
        Note originalNote;
        int lane;
        String word;
        float currentY;
        long targetTime;
        long spawnTime = System.currentTimeMillis();
        boolean isHit;
    }
    
    public interface GameViewListener {
        void onNoteHit(Note note, HitResult result);
        void onNoteMiss(Note note);
    }
}
