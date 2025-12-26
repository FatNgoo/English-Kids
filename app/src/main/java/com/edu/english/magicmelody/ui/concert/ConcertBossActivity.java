package com.edu.english.magicmelody.ui.concert;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.databinding.ActivityConcertBossBinding;
import com.edu.english.magicmelody.core.video.RecorderController;
import com.edu.english.magicmelody.core.video.ScreenRecorderService;
import com.edu.english.magicmelody.data.model.Lesson;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * ConcertBossActivity - Boss battle concert mode
 * Camera preview with mic RMS detection for voice input
 */
public class ConcertBossActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private static final int REQUEST_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    
    private ActivityConcertBossBinding binding;
    private ConcertBossViewModel viewModel;
    
    // Camera
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    
    // Mic
    private MediaRecorder mediaRecorder;
    private Handler micHandler = new Handler(Looper.getMainLooper());
    private Runnable micRunnable;
    private boolean isMicListening = false;
    
    // TTS
    private TextToSpeech tts;
    private boolean ttsReady = false;
    
    // Recording
    private RecorderController recorderController;
    
    // Vocab display
    private int currentVocabIndex = 0;
    private String[] vocabList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConcertBossBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupViewModel();
        setupTTS();
        setupRecorderController();
        
        if (allPermissionsGranted()) {
            setupCamera();
            setupViews();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
        }
        
        observeData();
        loadLessonData();
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ConcertBossViewModel.class);
    }
    
    private void setupTTS() {
        tts = new TextToSpeech(this, this);
    }
    
    private void setupRecorderController() {
        recorderController = new RecorderController(this);
        recorderController.setCallback(new ScreenRecorderService.RecordingCallback() {
            @Override
            public void onRecordingStarted(String filePath) {
                binding.btnRecord.setText("⏹ Dừng");
                binding.txtRecordingStatus.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onRecordingStopped(String filePath, boolean success) {
                binding.btnRecord.setText("⏺ Ghi hình");
                binding.txtRecordingStatus.setVisibility(View.GONE);
                Toast.makeText(ConcertBossActivity.this, 
                        "Video saved: " + filePath, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onRecordingError(String error) {
                binding.btnRecord.setText("⏺ Ghi hình");
                binding.txtRecordingStatus.setVisibility(View.GONE);
                Toast.makeText(ConcertBossActivity.this, 
                        "Recording error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadLessonData() {
        int lessonId = getIntent().getIntExtra("lessonId", 1);
        String theme = getIntent().getStringExtra("theme");
        if (theme == null) theme = "NATURE";
        
        viewModel.loadLesson(lessonId, theme);
    }
    
    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraPreview() {
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> showExitDialog());
        
        // Record button
        binding.btnRecord.setOnClickListener(v -> toggleRecording());
        
        // Speak button
        binding.btnSpeak.setOnClickListener(v -> {
            if (ttsReady && vocabList != null && currentVocabIndex < vocabList.length) {
                tts.speak(vocabList[currentVocabIndex], TextToSpeech.QUEUE_FLUSH, null, "vocab");
            }
        });
        
        // Start button
        binding.btnStart.setOnClickListener(v -> startBattle());
        
        // Initially hide game elements
        binding.gameContainer.setVisibility(View.GONE);
        binding.btnStart.setVisibility(View.VISIBLE);
    }
    
    private void observeData() {
        // Boss HP
        viewModel.getBossHp().observe(this, hp -> {
            if (hp != null) {
                binding.progressBossHp.setProgress(hp);
                
                // Animate HP bar color change
                if (hp < 30) {
                    binding.progressBossHp.setProgressTintList(
                            ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
                } else if (hp < 60) {
                    binding.progressBossHp.setProgressTintList(
                            ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark));
                }
            }
        });
        
        // Score
        viewModel.getScore().observe(this, score -> {
            if (score != null) {
                binding.txtScore.setText("Điểm: " + score);
            }
        });
        
        // Current vocab
        viewModel.getCurrentVocab().observe(this, vocab -> {
            if (vocab != null) {
                binding.txtVocab.setText(vocab);
                animateVocabAppear();
            }
        });
        
        // Game state
        viewModel.getGameState().observe(this, state -> {
            if (state == null) return;
            
            switch (state) {
                case IDLE:
                    binding.btnStart.setVisibility(View.VISIBLE);
                    binding.gameContainer.setVisibility(View.GONE);
                    break;
                    
                case PLAYING:
                    binding.btnStart.setVisibility(View.GONE);
                    binding.gameContainer.setVisibility(View.VISIBLE);
                    break;
                    
                case WON:
                    showVictoryDialog();
                    break;
                    
                case LOST:
                    showDefeatDialog();
                    break;
            }
        });
        
        // Mic level
        viewModel.getMicLevel().observe(this, level -> {
            if (level != null) {
                binding.progressMicLevel.setProgress(level);
                
                // Check if shouting loud enough
                if (level > 70 && viewModel.getGameState().getValue() == ConcertBossViewModel.GameState.PLAYING) {
                    viewModel.dealDamage(5);
                    animateHit();
                }
            }
        });
    }
    
    private void startBattle() {
        viewModel.startGame();
        startMicListening();
        
        // Start vocab rotation
        vocabList = viewModel.getVocabList();
        if (vocabList != null && vocabList.length > 0) {
            currentVocabIndex = 0;
            viewModel.setCurrentVocab(vocabList[0]);
            
            // Rotate vocab every 5 seconds
            Handler vocabHandler = new Handler(Looper.getMainLooper());
            Runnable vocabRunnable = new Runnable() {
                @Override
                public void run() {
                    if (viewModel.getGameState().getValue() == ConcertBossViewModel.GameState.PLAYING) {
                        currentVocabIndex = (currentVocabIndex + 1) % vocabList.length;
                        viewModel.setCurrentVocab(vocabList[currentVocabIndex]);
                        vocabHandler.postDelayed(this, 5000);
                    }
                }
            };
            vocabHandler.postDelayed(vocabRunnable, 5000);
        }
    }
    
    private void startMicListening() {
        if (!isMicListening) {
            isMicListening = true;
            
            // Using AudioRecord would be better, but MediaRecorder is simpler for amplitude
            micRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isMicListening) {
                        // Simulate mic level (in production, use AudioRecord)
                        // For now, use random simulation
                        int level = (int) (Math.random() * 100);
                        viewModel.setMicLevel(level);
                        micHandler.postDelayed(this, 100);
                    }
                }
            };
            micHandler.post(micRunnable);
        }
    }
    
    private void stopMicListening() {
        isMicListening = false;
        if (micRunnable != null) {
            micHandler.removeCallbacks(micRunnable);
        }
    }
    
    private void animateVocabAppear() {
        binding.txtVocab.setScaleX(0f);
        binding.txtVocab.setScaleY(0f);
        binding.txtVocab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }
    
    private void animateHit() {
        // Flash effect on boss
        binding.imgBoss.animate()
                .alpha(0.3f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.imgBoss.animate()
                            .alpha(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
        
        // Shake camera overlay
        ValueAnimator shakeAnimator = ValueAnimator.ofFloat(0, 10, -10, 5, -5, 0);
        shakeAnimator.setDuration(200);
        shakeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            binding.cameraOverlay.setTranslationX(value);
        });
        shakeAnimator.start();
    }
    
    private void toggleRecording() {
        if (recorderController.isRecording()) {
            recorderController.stopRecording();
        } else {
            // Request screen capture permission first
            recorderController.requestScreenCapturePermission(this);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RecorderController.REQUEST_CODE_SCREEN_CAPTURE) {
            recorderController.startRecording(resultCode, data);
        }
    }
    
    private void showVictoryDialog() {
        stopMicListening();
        
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chiến thắng!")
                .setMessage("Bạn đã đánh bại Boss!\nĐiểm: " + viewModel.getScore().getValue())
                .setPositiveButton("Tiếp tục", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
    
    private void showDefeatDialog() {
        stopMicListening();
        
        new AlertDialog.Builder(this)
                .setTitle("💔 Thất bại!")
                .setMessage("Boss quá mạnh!\nHãy thử lại nhé!")
                .setPositiveButton("Thử lại", (d, w) -> {
                    viewModel.resetGame();
                })
                .setNegativeButton("Thoát", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
    
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát?")
                .setMessage("Bạn có chắc muốn thoát?")
                .setPositiveButton("Thoát", (d, w) -> finish())
                .setNegativeButton("Ở lại", null)
                .show();
    }
    
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setupCamera();
                setupViews();
            } else {
                Toast.makeText(this, "Cần cấp quyền Camera và Mic", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA 
                    && result != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (cameraProvider != null) {
            bindCameraPreview();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopMicListening();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (recorderController != null && recorderController.isRecording()) {
            recorderController.stopRecording();
        }
        stopMicListening();
        super.onDestroy();
        binding = null;
    }
}
