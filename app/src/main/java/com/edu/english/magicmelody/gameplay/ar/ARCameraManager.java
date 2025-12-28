package com.edu.english.magicmelody.gameplay.ar;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 📷 AR Camera Manager
 * 
 * Manages CameraX for AR boss battles:
 * - Camera preview
 * - Face detection (optional)
 * - AR overlay support
 */
public class ARCameraManager {
    
    private static final String TAG = "ARCameraManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public enum CameraState {
        IDLE,
        INITIALIZING,
        RUNNING,
        PAUSED,
        ERROR
    }
    
    private Context context;
    private LifecycleOwner lifecycleOwner;
    private PreviewView previewView;
    
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;
    
    private ExecutorService analysisExecutor;
    private CameraState currentState = CameraState.IDLE;
    
    // Configuration
    private boolean useFrontCamera = true;
    private Size targetResolution = new Size(640, 480);
    private boolean enableAnalysis = false;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface ARCameraListener {
        void onCameraReady();
        void onCameraError(String error);
        void onFrameAnalyzed(FrameAnalysisResult result);
    }
    
    public static class FrameAnalysisResult {
        public final long timestampMs;
        public final int width;
        public final int height;
        public final boolean faceDetected;
        public final float[] facePosition;  // [x, y, width, height] normalized
        public final float brightness;
        
        public FrameAnalysisResult(long timestamp, int w, int h, 
                                   boolean face, float[] pos, float brightness) {
            this.timestampMs = timestamp;
            this.width = w;
            this.height = h;
            this.faceDetected = face;
            this.facePosition = pos;
            this.brightness = brightness;
        }
    }
    
    private ARCameraListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public ARCameraManager(Context context) {
        this.context = context.getApplicationContext();
        this.analysisExecutor = Executors.newSingleThreadExecutor();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(ARCameraListener listener) {
        this.listener = listener;
    }
    
    public void setUseFrontCamera(boolean useFront) {
        this.useFrontCamera = useFront;
    }
    
    public void setTargetResolution(int width, int height) {
        this.targetResolution = new Size(width, height);
    }
    
    public void setEnableAnalysis(boolean enable) {
        this.enableAnalysis = enable;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📷 CAMERA LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize camera with preview view
     */
    public void initialize(PreviewView previewView, LifecycleOwner owner) {
        this.previewView = previewView;
        this.lifecycleOwner = owner;
        
        currentState = CameraState.INITIALIZING;
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(context);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                setupCamera();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to initialize camera", e);
                currentState = CameraState.ERROR;
                if (listener != null) {
                    listener.onCameraError("Camera initialization failed: " + e.getMessage());
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }
    
    /**
     * Setup camera use cases
     */
    private void setupCamera() {
        if (cameraProvider == null) return;
        
        // Unbind any existing use cases
        cameraProvider.unbindAll();
        
        // Camera selector
        cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(useFrontCamera 
                ? CameraSelector.LENS_FACING_FRONT 
                : CameraSelector.LENS_FACING_BACK)
            .build();
        
        // Preview
        preview = new Preview.Builder()
            .setTargetResolution(targetResolution)
            .build();
        
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        try {
            if (enableAnalysis) {
                // Image analysis
                imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetResolution(targetResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
                
                imageAnalysis.setAnalyzer(analysisExecutor, this::analyzeFrame);
                
                // Bind with analysis
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    cameraSelector, 
                    preview, 
                    imageAnalysis
                );
            } else {
                // Bind preview only
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    cameraSelector, 
                    preview
                );
            }
            
            currentState = CameraState.RUNNING;
            
            if (listener != null) {
                listener.onCameraReady();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind camera", e);
            currentState = CameraState.ERROR;
            if (listener != null) {
                listener.onCameraError("Failed to start camera: " + e.getMessage());
            }
        }
    }
    
    /**
     * Analyze camera frame
     */
    private void analyzeFrame(@NonNull ImageProxy image) {
        long timestamp = System.currentTimeMillis();
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Calculate average brightness
        float brightness = calculateBrightness(image);
        
        // Face detection would require ML Kit or similar
        // For now, we'll just report frame info
        boolean faceDetected = false;
        float[] facePosition = null;
        
        FrameAnalysisResult result = new FrameAnalysisResult(
            timestamp, width, height, faceDetected, facePosition, brightness
        );
        
        if (listener != null) {
            listener.onFrameAnalyzed(result);
        }
        
        // Must close image when done
        image.close();
    }
    
    /**
     * Calculate average brightness from Y plane
     */
    private float calculateBrightness(ImageProxy image) {
        try {
            ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
            java.nio.ByteBuffer buffer = yPlane.getBuffer();
            
            int total = 0;
            int count = 0;
            int step = 10; // Sample every 10th pixel for performance
            
            while (buffer.hasRemaining()) {
                if (count % step == 0) {
                    total += (buffer.get() & 0xFF);
                } else {
                    buffer.get(); // Skip
                }
                count++;
            }
            
            return (float) total / (count / step) / 255f;
        } catch (Exception e) {
            return 0.5f;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CAMERA CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Switch between front and back camera
     */
    public void switchCamera() {
        useFrontCamera = !useFrontCamera;
        setupCamera();
    }
    
    /**
     * Pause camera
     */
    public void pause() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            currentState = CameraState.PAUSED;
        }
    }
    
    /**
     * Resume camera
     */
    public void resume() {
        if (currentState == CameraState.PAUSED) {
            setupCamera();
        }
    }
    
    /**
     * Check if camera is running
     */
    public boolean isRunning() {
        return currentState == CameraState.RUNNING;
    }
    
    public CameraState getCurrentState() {
        return currentState;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📐 AR OVERLAY HELPERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get preview view dimensions for AR overlay positioning
     */
    public int[] getPreviewDimensions() {
        if (previewView == null) return new int[]{0, 0};
        return new int[]{previewView.getWidth(), previewView.getHeight()};
    }
    
    /**
     * Convert normalized coordinates to preview coordinates
     */
    public float[] normalizedToPreview(float normX, float normY) {
        int[] dims = getPreviewDimensions();
        
        // Account for front camera mirror
        float adjustedX = useFrontCamera ? (1 - normX) : normX;
        
        return new float[]{
            adjustedX * dims[0],
            normY * dims[1]
        };
    }
    
    /**
     * Check if front camera is being used
     */
    public boolean isUsingFrontCamera() {
        return useFrontCamera;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void destroy() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        
        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
            analysisExecutor = null;
        }
        
        listener = null;
        currentState = CameraState.IDLE;
    }
}
