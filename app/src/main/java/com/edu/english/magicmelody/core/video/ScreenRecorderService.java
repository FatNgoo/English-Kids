package com.edu.english.magicmelody.core.video;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ScreenRecorderService - Foreground service for screen recording
 * Uses MediaProjection + MediaMuxer for recording
 * Includes fallback for mic-only audio on older devices
 */
public class ScreenRecorderService extends Service {
    
    private static final String TAG = "ScreenRecorder";
    private static final String CHANNEL_ID = "magic_melody_recorder";
    private static final int NOTIFICATION_ID = 1001;
    
    // Actions
    public static final String ACTION_START = "com.edu.english.magicmelody.START_RECORDING";
    public static final String ACTION_STOP = "com.edu.english.magicmelody.STOP_RECORDING";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";
    
    // Recording parameters
    private static final int VIDEO_FRAME_RATE = 30;
    private static final int VIDEO_BITRATE = 6_000_000;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BITRATE = 128000;
    
    // Components
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;
    private MediaMuxer mediaMuxer;
    
    // State
    private boolean isRecording = false;
    private String outputPath;
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    
    // Callbacks
    private static RecordingCallback callback;
    
    public interface RecordingCallback {
        void onRecordingStarted(String filePath);
        void onRecordingStopped(String filePath, boolean success);
        void onRecordingError(String error);
    }
    
    public static void setCallback(RecordingCallback cb) {
        callback = cb;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        
        String action = intent.getAction();
        
        if (ACTION_START.equals(action)) {
            int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED);
            Intent resultData = intent.getParcelableExtra(EXTRA_RESULT_DATA);
            
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                startForeground(NOTIFICATION_ID, createNotification());
                startRecording(resultCode, resultData);
            } else {
                notifyError("Permission denied");
                stopSelf();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopRecording();
            stopForeground(true);
            stopSelf();
        }
        
        return START_NOT_STICKY;
    }
    
    private void startRecording(int resultCode, Intent resultData) {
        if (isRecording) return;
        
        try {
            // Get screen metrics
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            
            // Use lower resolution for better performance
            screenWidth = Math.min(metrics.widthPixels, 720);
            screenHeight = (int) (screenWidth * ((float) metrics.heightPixels / metrics.widthPixels));
            screenDensity = metrics.densityDpi;
            
            // Generate output path
            outputPath = generateOutputPath();
            
            // Setup MediaRecorder
            setupMediaRecorder();
            
            // Create MediaProjection
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
            
            if (mediaProjection == null) {
                notifyError("Failed to create MediaProjection");
                return;
            }
            
            // Create VirtualDisplay
            Surface surface = mediaRecorder.getSurface();
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "MagicMelodyRecording",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            );
            
            // Start recording
            mediaRecorder.start();
            isRecording = true;
            
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    callback.onRecordingStarted(outputPath));
            }
            
            Log.d(TAG, "Recording started: " + outputPath);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start recording", e);
            notifyError(e.getMessage());
            cleanup();
        }
    }
    
    private void setupMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        
        // Audio source - mic fallback
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        
        // Video settings
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(screenWidth, screenHeight);
        mediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        mediaRecorder.setVideoEncodingBitRate(VIDEO_BITRATE);
        
        // Audio settings
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE);
        mediaRecorder.setAudioEncodingBitRate(AUDIO_BITRATE);
        
        mediaRecorder.setOutputFile(outputPath);
        mediaRecorder.prepare();
    }
    
    private void stopRecording() {
        if (!isRecording) return;
        
        isRecording = false;
        boolean success = true;
        
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping recorder", e);
            success = false;
        }
        
        cleanup();
        
        // Add to MediaStore
        if (success && outputPath != null) {
            addToMediaStore(outputPath);
        }
        
        final boolean finalSuccess = success;
        final String finalPath = outputPath;
        
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                callback.onRecordingStopped(finalPath, finalSuccess));
        }
        
        Log.d(TAG, "Recording stopped: " + (success ? "success" : "failed"));
    }
    
    private void cleanup() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                // Ignore
            }
            mediaRecorder = null;
        }
    }
    
    private String generateOutputPath() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String filename = "MagicMelody_" + timestamp + ".mp4";
        
        // Use app-specific directory for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (dir != null) {
                if (!dir.exists()) dir.mkdirs();
                return new File(dir, filename).getAbsolutePath();
            }
        }
        
        // Fallback to external storage
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File subDir = new File(dir, "MagicMelody");
        if (!subDir.exists()) subDir.mkdirs();
        return new File(subDir, filename).getAbsolutePath();
    }
    
    private void addToMediaStore(String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, new File(filePath).getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MagicMelody");
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            
            try {
                Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                Log.d(TAG, "Added to MediaStore: " + uri);
            } catch (Exception e) {
                Log.e(TAG, "Failed to add to MediaStore", e);
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Magic Melody Recording",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Recording in progress");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent stopIntent = new Intent(this, ScreenRecorderService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang quay video")
            .setContentText("Magic Melody đang ghi hình...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(android.R.drawable.ic_media_pause, "Dừng", stopPendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void notifyError(String error) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                callback.onRecordingError(error));
        }
    }
    
    @Override
    public void onDestroy() {
        cleanup();
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
