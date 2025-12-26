package com.edu.english.magicmelody.core.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

/**
 * RecorderController - Controller for screen recording functionality
 * Manages the ScreenRecorderService and permissions
 */
public class RecorderController {
    
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 1001;
    
    private final Context context;
    private boolean isRecording = false;
    private String currentRecordingPath;
    
    // Callback
    private ScreenRecorderService.RecordingCallback callback;
    
    public RecorderController(Context context) {
        this.context = context;
    }
    
    /**
     * Request screen capture permission
     * Must be called from an Activity
     * @param activity The activity to request from
     */
    public void requestScreenCapturePermission(Activity activity) {
        MediaProjectionManager projectionManager = 
            (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        if (projectionManager != null) {
            Intent intent = projectionManager.createScreenCaptureIntent();
            activity.startActivityForResult(intent, REQUEST_CODE_SCREEN_CAPTURE);
        }
    }
    
    /**
     * Start recording with permission result
     * Call this from Activity.onActivityResult()
     * @param resultCode Result code from permission request
     * @param data Intent data from permission request
     */
    public void startRecording(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            if (callback != null) {
                callback.onRecordingError("Permission denied");
            }
            return;
        }
        
        // Set callback
        ScreenRecorderService.setCallback(new ScreenRecorderService.RecordingCallback() {
            @Override
            public void onRecordingStarted(String filePath) {
                isRecording = true;
                currentRecordingPath = filePath;
                if (callback != null) {
                    callback.onRecordingStarted(filePath);
                }
            }
            
            @Override
            public void onRecordingStopped(String filePath, boolean success) {
                isRecording = false;
                if (callback != null) {
                    callback.onRecordingStopped(filePath, success);
                }
            }
            
            @Override
            public void onRecordingError(String error) {
                isRecording = false;
                if (callback != null) {
                    callback.onRecordingError(error);
                }
            }
        });
        
        // Start service
        Intent serviceIntent = new Intent(context, ScreenRecorderService.class);
        serviceIntent.setAction(ScreenRecorderService.ACTION_START);
        serviceIntent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode);
        serviceIntent.putExtra(ScreenRecorderService.EXTRA_RESULT_DATA, data);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
    
    /**
     * Stop current recording
     */
    public void stopRecording() {
        Intent serviceIntent = new Intent(context, ScreenRecorderService.class);
        serviceIntent.setAction(ScreenRecorderService.ACTION_STOP);
        context.startService(serviceIntent);
    }
    
    /**
     * Check if currently recording
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Get current recording path
     */
    public String getCurrentRecordingPath() {
        return currentRecordingPath;
    }
    
    /**
     * Set recording callback
     */
    public void setCallback(ScreenRecorderService.RecordingCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Check if screen capture is supported
     */
    public boolean isScreenCaptureSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
    
    /**
     * Check if internal audio capture is supported (Android 10+)
     */
    public boolean isInternalAudioCaptureSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
