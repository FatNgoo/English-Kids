package com.edu.english.magicmelody.ui.notebook;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.edu.english.magicmelody.core.audio.ToneEngine;
import com.edu.english.magicmelody.data.model.CollectionEntity;
import com.edu.english.databinding.ActivityNotebookBinding;

import java.util.ArrayList;
import java.util.Locale;

/**
 * NotebookActivity - Magic Notebook collection screen
 * Displays all collected vocabulary with TTS and melody replay
 */
public class NotebookActivity extends AppCompatActivity implements 
        CollectionAdapter.OnItemClickListener, TextToSpeech.OnInitListener {
    
    private ActivityNotebookBinding binding;
    private NotebookViewModel viewModel;
    private CollectionAdapter adapter;
    private TextToSpeech tts;
    private ToneEngine toneEngine;
    private boolean ttsInitialized = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotebookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupViewModel();
        setupTTS();
        setupToneEngine();
        setupViews();
        observeData();
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotebookViewModel.class);
    }
    
    private void setupTTS() {
        tts = new TextToSpeech(this, this);
    }
    
    private void setupToneEngine() {
        toneEngine = new ToneEngine();
    }
    
    private void setupViews() {
        // RecyclerView grid
        adapter = new CollectionAdapter(new ArrayList<>(), this);
        binding.recyclerViewCollection.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewCollection.setAdapter(adapter);
        
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Filter buttons
        binding.btnFilterAll.setOnClickListener(v -> viewModel.setFilter(null));
        binding.btnFilterNature.setOnClickListener(v -> viewModel.setFilter("NATURE"));
        binding.btnFilterFantasy.setOnClickListener(v -> viewModel.setFilter("FANTASY"));
        binding.btnFilterScifi.setOnClickListener(v -> viewModel.setFilter("SCIFI"));
        
        // Detail panel - initially hidden
        binding.detailPanel.setVisibility(View.GONE);
        binding.detailPanel.setOnClickListener(v -> hideDetailPanel());
        binding.btnPlayVocab.setOnClickListener(v -> speakCurrentVocab());
        binding.btnPlayMelody.setOnClickListener(v -> playCurrentMelody());
    }
    
    private void observeData() {
        // Collection items
        viewModel.getCollections().observe(this, collections -> {
            if (collections != null && !collections.isEmpty()) {
                adapter.updateItems(collections);
                binding.txtEmptyMessage.setVisibility(View.GONE);
                binding.recyclerViewCollection.setVisibility(View.VISIBLE);
            } else {
                binding.txtEmptyMessage.setVisibility(View.VISIBLE);
                binding.recyclerViewCollection.setVisibility(View.GONE);
            }
        });
        
        // Collection count
        viewModel.getCollectionCount().observe(this, count -> {
            if (count != null) {
                binding.txtCollectionCount.setText("Bộ sưu tập: " + count + " từ vựng");
            }
        });
        
        // Selected item
        viewModel.getSelectedItem().observe(this, item -> {
            if (item != null) {
                showDetailPanel(item);
            }
        });
    }
    
    @Override
    public void onItemClick(CollectionEntity item) {
        viewModel.selectItem(item);
    }
    
    private void showDetailPanel(CollectionEntity item) {
        binding.detailPanel.setVisibility(View.VISIBLE);
        binding.txtDetailVocab.setText(item.getVocab());
        binding.txtDetailVietnamese.setText(item.getVocabVietnamese());
        binding.txtDetailTheme.setText("Chủ đề: " + item.getTheme());
        
        // Animate in
        binding.detailPanel.setAlpha(0f);
        binding.detailPanel.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        
        binding.cardDetail.setScaleX(0.8f);
        binding.cardDetail.setScaleY(0.8f);
        binding.cardDetail.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
        
        // Auto-speak
        speakCurrentVocab();
    }
    
    private void hideDetailPanel() {
        binding.detailPanel.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    binding.detailPanel.setVisibility(View.GONE);
                    viewModel.clearSelection();
                })
                .start();
    }
    
    private void speakCurrentVocab() {
        CollectionEntity item = viewModel.getSelectedItem().getValue();
        if (item == null) return;
        
        if (ttsInitialized) {
            tts.speak(item.getVocab(), TextToSpeech.QUEUE_FLUSH, null, "vocab");
        } else {
            Toast.makeText(this, "TTS chưa sẵn sàng", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void playCurrentMelody() {
        CollectionEntity item = viewModel.getSelectedItem().getValue();
        if (item == null) return;
        
        // Play a simple melody based on vocab
        // Generate melody from vocab characters
        String vocab = item.getVocab().toLowerCase();
        int[] notes = new int[Math.min(vocab.length(), 4)];
        
        for (int i = 0; i < notes.length; i++) {
            char c = vocab.charAt(i);
            notes[i] = (c - 'a') % 7; // Map to notes 0-6
        }
        
        toneEngine.playMelody(notes, 300, null);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try Vietnamese
                tts.setLanguage(new Locale("vi", "VN"));
            }
            ttsInitialized = true;
        }
    }
    
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (toneEngine != null) {
            toneEngine.release();
        }
        super.onDestroy();
        binding = null;
    }
}
