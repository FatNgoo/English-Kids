package com.edu.english.coloralchemy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Sensor Manager Handler
 * Manages accelerometer for tilt and shake detection
 */
public class SensorManagerHandler implements SensorEventListener {
    
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    
    // Current accelerometer values
    private float accelX, accelY, accelZ;
    
    // Shake detection
    private float lastAccelX, lastAccelY, lastAccelZ;
    private float shakeThreshold = 12.0f;
    private float shakeIntensity;
    private long lastShakeTime;
    private static final long SHAKE_COOLDOWN = 200; // ms
    
    // Smoothing
    private static final float ALPHA = 0.15f; // Low-pass filter coefficient
    private float filteredX, filteredY, filteredZ;
    
    // Callbacks
    private OnSensorUpdateListener listener;
    
    /**
     * Listener interface for sensor updates
     */
    public interface OnSensorUpdateListener {
        void onAccelerometerUpdate(float x, float y, float z);
        void onShakeDetected(float intensity);
    }
    
    public SensorManagerHandler(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        
        if (sensorManager != null) {
            this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        
        // Initialize values
        accelX = accelY = accelZ = 0;
        lastAccelX = lastAccelY = lastAccelZ = 0;
        filteredX = filteredY = filteredZ = 0;
        shakeIntensity = 0;
        lastShakeTime = 0;
    }
    
    /**
     * Start listening to sensors
     */
    public void start() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            );
        }
    }
    
    /**
     * Stop listening to sensors
     */
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    
    /**
     * Set the listener for sensor updates
     */
    public void setListener(OnSensorUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Set shake detection threshold
     */
    public void setShakeThreshold(float threshold) {
        this.shakeThreshold = threshold;
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        
        // Get raw values
        float rawX = event.values[0];
        float rawY = event.values[1];
        float rawZ = event.values[2];
        
        // Apply low-pass filter for smooth tilt
        filteredX = filteredX + ALPHA * (rawX - filteredX);
        filteredY = filteredY + ALPHA * (rawY - filteredY);
        filteredZ = filteredZ + ALPHA * (rawZ - filteredZ);
        
        accelX = filteredX;
        accelY = filteredY;
        accelZ = filteredZ;
        
        // Notify listener of accelerometer update
        if (listener != null) {
            listener.onAccelerometerUpdate(accelX, accelY, accelZ);
        }
        
        // Detect shake
        detectShake(rawX, rawY, rawZ);
        
        // Store for next comparison
        lastAccelX = rawX;
        lastAccelY = rawY;
        lastAccelZ = rawZ;
    }
    
    /**
     * Detect shake motion
     */
    private void detectShake(float x, float y, float z) {
        long currentTime = System.currentTimeMillis();
        
        // Calculate acceleration change
        float deltaX = Math.abs(x - lastAccelX);
        float deltaY = Math.abs(y - lastAccelY);
        float deltaZ = Math.abs(z - lastAccelZ);
        
        float totalDelta = deltaX + deltaY + deltaZ;
        
        // Update shake intensity (smoothed)
        if (totalDelta > shakeThreshold) {
            shakeIntensity = Math.min(1.0f, (totalDelta - shakeThreshold) / 20.0f);
            
            // Check cooldown
            if (currentTime - lastShakeTime > SHAKE_COOLDOWN) {
                lastShakeTime = currentTime;
                
                if (listener != null) {
                    listener.onShakeDetected(shakeIntensity);
                }
            }
        } else {
            // Decay shake intensity
            shakeIntensity *= 0.9f;
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }
    
    // ==================== Getters ====================
    
    public float getAccelX() { return accelX; }
    public float getAccelY() { return accelY; }
    public float getAccelZ() { return accelZ; }
    
    public float getShakeIntensity() { return shakeIntensity; }
    
    /**
     * Get gravity-adjusted X for horizontal tilt
     * Returns value suitable for liquid simulation
     */
    public float getTiltX() {
        // Normalize to roughly -1 to 1 range
        return EasingFunctions.clamp(accelX / 10.0f, -1.0f, 1.0f);
    }
    
    /**
     * Get gravity-adjusted Y for vertical tilt
     */
    public float getTiltY() {
        // Account for gravity on Y axis (device held upright = ~9.8)
        return EasingFunctions.clamp((accelY - 9.8f) / 10.0f, -1.0f, 1.0f);
    }
    
    /**
     * Check if sensors are available
     */
    public boolean isSensorAvailable() {
        return accelerometer != null;
    }
}
