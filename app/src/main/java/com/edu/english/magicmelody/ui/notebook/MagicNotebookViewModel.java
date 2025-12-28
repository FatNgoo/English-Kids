package com.edu.english.magicmelody.ui.notebook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.edu.english.magicmelody.data.entity.CollectedNote;
import com.edu.english.magicmelody.data.repository.CollectedNoteRepository;
import com.edu.english.magicmelody.data.repository.UserProfileRepository;

import java.util.List;

/**
 * 📓 Magic Notebook ViewModel
 * 
 * Purpose: Handle Magic Notebook screen logic
 * - Display collected notes
 * - Filter by type/world
 * - Play notes
 * - Manage favorites
 */
public class MagicNotebookViewModel extends AndroidViewModel {
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    private final CollectedNoteRepository collectedNoteRepository;
    private final UserProfileRepository userProfileRepository;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LIVE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private final MutableLiveData<Long> userId = new MutableLiveData<>();
    private final MutableLiveData<FilterType> currentFilter = new MutableLiveData<>(FilterType.ALL);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<CollectedNote> selectedNote = new MutableLiveData<>();
    private final MutableLiveData<PlayNoteEvent> playNoteEvent = new MutableLiveData<>();
    
    private LiveData<List<CollectedNote>> allNotes;
    private LiveData<List<CollectedNote>> filteredNotes;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ENUMS & EVENTS
    // ═══════════════════════════════════════════════════════════════
    
    public enum FilterType {
        ALL,
        FAVORITES,
        BASIC,
        SPECIAL,
        LEGENDARY,
        MASTERED
    }
    
    public static class PlayNoteEvent {
        public final String noteName;
        public final String audioFile;
        public final String word;
        
        public PlayNoteEvent(String noteName, String audioFile, String word) {
            this.noteName = noteName;
            this.audioFile = audioFile;
            this.word = word;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public MagicNotebookViewModel(@NonNull Application application) {
        super(application);
        
        collectedNoteRepository = new CollectedNoteRepository(application);
        userProfileRepository = new UserProfileRepository(application);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<List<CollectedNote>> getAllNotes() {
        return allNotes;
    }
    
    public LiveData<List<CollectedNote>> getFilteredNotes() {
        return filteredNotes;
    }
    
    public LiveData<FilterType> getCurrentFilter() {
        return currentFilter;
    }
    
    public LiveData<CollectedNote> getSelectedNote() {
        return selectedNote;
    }
    
    public LiveData<PlayNoteEvent> getPlayNoteEvent() {
        return playNoteEvent;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 ACTIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize with user ID
     */
    public void initialize(long userId) {
        this.userId.setValue(userId);
        
        // Load all notes
        allNotes = collectedNoteRepository.getAllNotesForUser(userId);
        
        // Set up filtered notes based on current filter
        updateFilteredNotes();
    }
    
    /**
     * Update filtered notes based on current filter
     */
    private void updateFilteredNotes() {
        Long uid = userId.getValue();
        if (uid == null) return;
        
        FilterType filter = currentFilter.getValue();
        if (filter == null) filter = FilterType.ALL;
        
        switch (filter) {
            case FAVORITES:
                filteredNotes = collectedNoteRepository.getFavoriteNotes(uid);
                break;
            case BASIC:
                filteredNotes = collectedNoteRepository.getNotesByType(uid, "basic");
                break;
            case SPECIAL:
                filteredNotes = collectedNoteRepository.getNotesByType(uid, "special");
                break;
            case LEGENDARY:
                filteredNotes = collectedNoteRepository.getLegendaryNotes(uid);
                break;
            case MASTERED:
                filteredNotes = collectedNoteRepository.getMasteredNotes(uid);
                break;
            case ALL:
            default:
                filteredNotes = allNotes;
                break;
        }
    }
    
    /**
     * Set filter type
     */
    public void setFilter(FilterType filter) {
        currentFilter.setValue(filter);
        updateFilteredNotes();
    }
    
    /**
     * Search notes by word
     */
    public void searchNotes(String query) {
        searchQuery.setValue(query);
        
        Long uid = userId.getValue();
        if (uid == null) return;
        
        if (query == null || query.isEmpty()) {
            updateFilteredNotes();
        } else {
            filteredNotes = collectedNoteRepository.searchNotesByWord(uid, query);
        }
    }
    
    /**
     * Select a note to view details
     */
    public void selectNote(CollectedNote note) {
        selectedNote.setValue(note);
    }
    
    /**
     * Clear selected note
     */
    public void clearSelection() {
        selectedNote.setValue(null);
    }
    
    /**
     * Play a note sound
     */
    public void playNote(CollectedNote note) {
        // Increment play count
        collectedNoteRepository.playNote(note.getNoteId());
        
        // Check if should mark as mastered
        if (note.getPlayCount() >= 9) { // Will be 10 after increment
            collectedNoteRepository.markAsMastered(note.getNoteId());
        }
        
        // Emit play event for audio
        playNoteEvent.setValue(new PlayNoteEvent(
            note.getNoteName(),
            note.getAudioFile(),
            note.getWord()
        ));
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(CollectedNote note) {
        collectedNoteRepository.toggleFavorite(note.getNoteId());
    }
    
    /**
     * Get statistics
     */
    public NotebookStats getStats() {
        Long uid = userId.getValue();
        if (uid == null) return new NotebookStats();
        
        int total = collectedNoteRepository.getTotalNoteCount(uid);
        int basic = collectedNoteRepository.getAllNotesForUserSync(uid).size(); // Simplified
        
        return new NotebookStats(total, 0, 0); // TODO: Calculate properly
    }
    
    /**
     * Notebook statistics
     */
    public static class NotebookStats {
        public final int totalNotes;
        public final int masteredNotes;
        public final int legendaryNotes;
        
        public NotebookStats() {
            this(0, 0, 0);
        }
        
        public NotebookStats(int total, int mastered, int legendary) {
            this.totalNotes = total;
            this.masteredNotes = mastered;
            this.legendaryNotes = legendary;
        }
    }
}
