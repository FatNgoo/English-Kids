package com.edu.english.alphabet_adventure.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom view that draws 5 lanes with dashed separator lines.
 */
public class LanesView extends View {

    private Paint linePaint;
    private Paint bgPaint;
    private int highlightedLane = 2;

    public LanesView(Context context) {
        super(context);
        init();
    }

    public LanesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LanesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#50FFFFFF"));
        linePaint.setStrokeWidth(2f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setPathEffect(new DashPathEffect(new float[]{20f, 10f}, 0f));

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#20FFFFFF"));
        bgPaint.setStyle(Paint.Style.FILL);
    }

    public void setHighlightedLane(int lane) {
        this.highlightedLane = lane;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float laneHeight = height / 5f;

        // Draw background
        canvas.drawRect(0, 0, width, height, bgPaint);

        // Draw highlighted lane
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#40FFEB3B"));
        highlightPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, highlightedLane * laneHeight, width, (highlightedLane + 1) * laneHeight, highlightPaint);

        // Draw lane separator lines
        for (int i = 1; i < 5; i++) {
            float y = i * laneHeight;
            canvas.drawLine(0, y, width, y, linePaint);
        }
    }
}
