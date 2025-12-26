package com.edu.english.magicmelody.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.edu.english.magicmelody.core.game.Note;
import com.edu.english.magicmelody.core.game.NotePool;

import java.util.List;

/**
 * RhythmTrackView - Custom view for rendering falling notes
 * Draws lanes, notes, and hit line with visual effects
 */
public class RhythmTrackView extends View {
    
    private static final int NUM_LANES = 7;
    private static final float HIT_LINE_POSITION = 0.85f; // 85% from top
    private static final float NOTE_SIZE_RATIO = 0.8f;   // Note size relative to lane width
    
    // Lane colors (matching piano keys)
    private static final int[] LANE_COLORS = {
        0x40FF5252,  // C - Red (translucent)
        0x40FF9800,  // D - Orange
        0x40FFEB3B,  // E - Yellow
        0x404CAF50,  // F - Green
        0x402196F3,  // G - Blue
        0x409C27B0,  // A - Purple
        0x40E91E63   // B - Pink
    };
    
    private static final int[] NOTE_COLORS = {
        0xFFFF5252,  // C - Red
        0xFFFF9800,  // D - Orange
        0xFFFFEB3B,  // E - Yellow
        0xFF4CAF50,  // F - Green
        0xFF2196F3,  // G - Blue
        0xFF9C27B0,  // A - Purple
        0xFFE91E63   // B - Pink
    };
    
    private Paint lanePaint;
    private Paint notePaint;
    private Paint hitLinePaint;
    private Paint glowPaint;
    private Paint textPaint;
    private RectF noteRect;
    private Path notePath;
    
    private NotePool notePool;
    private float laneWidth;
    private float hitLineY;
    
    // Visual effects
    private long lastHitTime;
    private int lastHitLane;
    private boolean showHitEffect;
    
    public RhythmTrackView(Context context) {
        super(context);
        init();
    }
    
    public RhythmTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public RhythmTrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Lane paint
        lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lanePaint.setStyle(Paint.Style.FILL);
        
        // Note paint
        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setStyle(Paint.Style.FILL);
        
        // Hit line paint
        hitLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitLinePaint.setStyle(Paint.Style.STROKE);
        hitLinePaint.setStrokeWidth(8f);
        hitLinePaint.setColor(Color.WHITE);
        
        // Glow paint for effects
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        // Text paint for vocab
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        noteRect = new RectF();
        notePath = new Path();
        
        // Disable hardware acceleration for better gradient rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    /**
     * Set the note pool to render
     */
    public void setNotePool(NotePool notePool) {
        this.notePool = notePool;
    }
    
    /**
     * Show hit effect at lane
     */
    public void showHitEffect(int lane) {
        lastHitLane = lane;
        lastHitTime = System.currentTimeMillis();
        showHitEffect = true;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;
        
        laneWidth = (float) width / NUM_LANES;
        hitLineY = height * HIT_LINE_POSITION;
        
        // Draw background gradient
        drawBackground(canvas, width, height);
        
        // Draw lanes
        drawLanes(canvas, width, height);
        
        // Draw hit line
        drawHitLine(canvas, width);
        
        // Draw notes
        if (notePool != null) {
            drawNotes(canvas, height);
        }
        
        // Draw hit effect
        if (showHitEffect) {
            drawHitEffect(canvas, height);
        }
    }
    
    private void drawBackground(Canvas canvas, int width, int height) {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, height,
            0xFF1a1a2e, 0xFF16213e,
            Shader.TileMode.CLAMP
        );
        
        Paint bgPaint = new Paint();
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, bgPaint);
    }
    
    private void drawLanes(Canvas canvas, int width, int height) {
        for (int i = 0; i < NUM_LANES; i++) {
            float left = i * laneWidth;
            float right = left + laneWidth;
            
            // Lane background with gradient
            LinearGradient laneGradient = new LinearGradient(
                0, 0, 0, height,
                LANE_COLORS[i], 0x10FFFFFF,
                Shader.TileMode.CLAMP
            );
            lanePaint.setShader(laneGradient);
            canvas.drawRect(left, 0, right, height, lanePaint);
            
            // Lane divider
            Paint dividerPaint = new Paint();
            dividerPaint.setColor(0x30FFFFFF);
            dividerPaint.setStrokeWidth(2f);
            canvas.drawLine(right, 0, right, height, dividerPaint);
        }
    }
    
    private void drawHitLine(Canvas canvas, int width) {
        // Glow effect
        glowPaint.setColor(0x40FFFFFF);
        canvas.drawRect(0, hitLineY - 20, width, hitLineY + 20, glowPaint);
        
        // Main line
        canvas.drawLine(0, hitLineY, width, hitLineY, hitLinePaint);
        
        // Hit zone indicators
        for (int i = 0; i < NUM_LANES; i++) {
            float centerX = i * laneWidth + laneWidth / 2;
            
            Paint zonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            zonePaint.setStyle(Paint.Style.STROKE);
            zonePaint.setStrokeWidth(3f);
            zonePaint.setColor(NOTE_COLORS[i]);
            
            float radius = laneWidth * 0.3f;
            canvas.drawCircle(centerX, hitLineY, radius, zonePaint);
        }
    }
    
    private void drawNotes(Canvas canvas, int height) {
        float noteWidth = laneWidth * NOTE_SIZE_RATIO;
        float noteHeight = noteWidth * 0.6f;
        
        textPaint.setTextSize(noteHeight * 0.4f);
        
        for (Note note : notePool.getAllNotes()) {
            if (!note.isInUse() || note.getState() == Note.STATE_INACTIVE) {
                continue;
            }
            
            int lane = note.getLane();
            float progress = note.getY();
            
            // Calculate note position
            float noteY = progress * hitLineY;
            float centerX = lane * laneWidth + laneWidth / 2;
            
            // Skip if off screen
            if (noteY < -noteHeight || noteY > height + noteHeight) {
                continue;
            }
            
            // Note color and alpha
            int baseColor = NOTE_COLORS[lane % NOTE_COLORS.length];
            int alpha = (int) (note.getAlpha() * 255);
            int noteColor = (alpha << 24) | (baseColor & 0x00FFFFFF);
            
            // Draw note glow
            if (note.isActive()) {
                RadialGradient glow = new RadialGradient(
                    centerX, noteY, noteWidth,
                    (0x60 << 24) | (baseColor & 0x00FFFFFF), 0x00000000,
                    Shader.TileMode.CLAMP
                );
                glowPaint.setShader(glow);
                canvas.drawCircle(centerX, noteY, noteWidth, glowPaint);
                glowPaint.setShader(null);
            }
            
            // Draw note shape (rounded rect with gradient)
            noteRect.set(
                centerX - noteWidth / 2,
                noteY - noteHeight / 2,
                centerX + noteWidth / 2,
                noteY + noteHeight / 2
            );
            
            LinearGradient noteGradient = new LinearGradient(
                noteRect.left, noteRect.top,
                noteRect.left, noteRect.bottom,
                lightenColor(baseColor, 1.3f),
                baseColor,
                Shader.TileMode.CLAMP
            );
            notePaint.setShader(noteGradient);
            notePaint.setAlpha(alpha);
            
            float cornerRadius = noteHeight / 3;
            canvas.drawRoundRect(noteRect, cornerRadius, cornerRadius, notePaint);
            
            // Draw note border
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2f);
            borderPaint.setColor(Color.WHITE);
            borderPaint.setAlpha(alpha);
            canvas.drawRoundRect(noteRect, cornerRadius, cornerRadius, borderPaint);
            
            // Draw vocab if available
            String vocab = note.getVocabKey();
            if (vocab != null && !vocab.isEmpty() && note.isActive()) {
                textPaint.setAlpha(alpha);
                canvas.drawText(vocab, centerX, noteY + textPaint.getTextSize() / 3, textPaint);
            }
        }
    }
    
    private void drawHitEffect(Canvas canvas, int height) {
        long elapsed = System.currentTimeMillis() - lastHitTime;
        
        if (elapsed > 300) {
            showHitEffect = false;
            return;
        }
        
        float progress = elapsed / 300f;
        float alpha = 1f - progress;
        float scale = 1f + progress * 0.5f;
        
        float centerX = lastHitLane * laneWidth + laneWidth / 2;
        float radius = laneWidth * 0.4f * scale;
        
        int color = NOTE_COLORS[lastHitLane % NOTE_COLORS.length];
        color = (((int) (alpha * 180)) << 24) | (color & 0x00FFFFFF);
        
        RadialGradient gradient = new RadialGradient(
            centerX, hitLineY, radius,
            color, 0x00000000,
            Shader.TileMode.CLAMP
        );
        
        glowPaint.setShader(gradient);
        canvas.drawCircle(centerX, hitLineY, radius, glowPaint);
        glowPaint.setShader(null);
        
        invalidate(); // Continue animation
    }
    
    private int lightenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = (int) Math.min(255, Color.red(color) * factor);
        int g = (int) Math.min(255, Color.green(color) * factor);
        int b = (int) Math.min(255, Color.blue(color) * factor);
        return Color.argb(a, r, g, b);
    }
}
