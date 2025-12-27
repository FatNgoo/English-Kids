package com.edu.english.storybook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.storybook.data.StoryDatabase;
import com.edu.english.storybook.data.StoryEntity;
import com.edu.english.storybook.data.StoryRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Library screen showing saved stories with search and filter.
 */
public class StoryLibraryActivity extends AppCompatActivity implements StoryLibraryAdapter.OnStoryClickListener {

    private RecyclerView rvStories;
    private View emptyState;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private ImageButton btnSearch;
    
    private StoryLibraryAdapter adapter;
    private StoryRepository repository;
    
    private List<StoryEntity> allStories = new ArrayList<>();
    private String currentFilter = null; // null means all
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_library);

        repository = new StoryRepository(this);

        bindViews();
        setupRecyclerView();
        setupListeners();
        loadStories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStories();
    }

    private void bindViews() {
        rvStories = findViewById(R.id.rv_stories);
        emptyState = findViewById(R.id.empty_state);
        tilSearch = findViewById(R.id.til_search);
        etSearch = findViewById(R.id.et_search);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        btnSearch = findViewById(R.id.btn_search);
        
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new StoryLibraryAdapter(this, this);
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> toggleSearch());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStories();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = null;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_all) {
                    currentFilter = null;
                } else if (checkedId == R.id.chip_novel) {
                    currentFilter = "NOVEL";
                } else if (checkedId == R.id.chip_fairy_tales) {
                    currentFilter = "FAIRY_TALES";
                } else if (checkedId == R.id.chip_world) {
                    currentFilter = "SEE_THE_WORLD";
                } else if (checkedId == R.id.chip_history) {
                    currentFilter = "HISTORY";
                }
            }
            filterStories();
        });
    }

    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        tilSearch.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
        if (!isSearchVisible) {
            etSearch.setText("");
        }
    }

    private void loadStories() {
        repository.getAllStories(stories -> {
            runOnUiThread(() -> {
                allStories = stories;
                filterStories();
            });
        });
    }

    private void filterStories() {
        List<StoryEntity> filtered = new ArrayList<>();
        String searchQuery = etSearch.getText() != null ? 
                            etSearch.getText().toString().toLowerCase().trim() : "";

        for (StoryEntity story : allStories) {
            boolean matchesCategory = currentFilter == null || 
                                      story.getCategory().equals(currentFilter);
            boolean matchesSearch = searchQuery.isEmpty() || 
                                    story.getTitle().toLowerCase().contains(searchQuery);

            if (matchesCategory && matchesSearch) {
                filtered.add(story);
            }
        }

        adapter.setStories(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvStories.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onStoryClick(StoryEntity story) {
        android.content.Intent intent = new android.content.Intent(this, StoryReaderActivity.class);
        intent.putExtra(StoryReaderActivity.EXTRA_STORY_JSON, story.getStoryJson());
        intent.putExtra(StoryReaderActivity.EXTRA_STORY_ID, story.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(StoryEntity story) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Story")
            .setMessage("Are you sure you want to delete \"" + story.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                repository.delete(story, () -> {
                    runOnUiThread(this::loadStories);
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
