package com.edu.english.magicmelody.gameplay.notebook;

import com.edu.english.magicmelody.data.entity.CollectedNote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 📔 Magic Notebook Manager
 * 
 * Manages the player's collection of words/notes:
 * - Collection organization
 * - Filtering and searching
 * - Statistics and achievements
 */
public class MagicNotebookManager {
    
    private static final String TAG = "MagicNotebookManager";
    
    // Singleton instance
    private static MagicNotebookManager instance;
    
    public static synchronized MagicNotebookManager getInstance() {
        if (instance == null) {
            instance = new MagicNotebookManager();
        }
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 FILTER & SORT ENUMS
    // ═══════════════════════════════════════════════════════════════
    
    public enum FilterType {
        ALL,
        BY_WORLD,
        BY_RARITY,
        BY_MASTERY,
        FAVORITES,
        RECENT,
        UNMASTERED
    }
    
    public enum SortType {
        ALPHABETICAL,
        RECENT_FIRST,
        OLDEST_FIRST,
        RARITY_HIGH,
        RARITY_LOW,
        MASTERY_HIGH,
        MASTERY_LOW,
        WORLD
    }
    
    public enum NoteRarity {
        COMMON("common", 1),
        UNCOMMON("uncommon", 2),
        RARE("rare", 3),
        EPIC("epic", 4),
        LEGENDARY("legendary", 5);
        
        private final String key;
        private final int tier;
        
        NoteRarity(String key, int tier) {
            this.key = key;
            this.tier = tier;
        }
        
        public String getKey() { return key; }
        public int getTier() { return tier; }
        
        public static NoteRarity fromString(String s) {
            for (NoteRarity r : values()) {
                if (r.key.equalsIgnoreCase(s)) return r;
            }
            return COMMON;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DISPLAY MODEL
    // ═══════════════════════════════════════════════════════════════
    
    public static class NotebookEntry {
        public String noteId;
        public String word;
        public String pronunciation;
        public String meaning;
        public String worldId;
        public String worldName;
        public NoteRarity rarity;
        public int masteryLevel;      // 0-100
        public int timesCollected;
        public int perfectHits;
        public long firstCollectedAt;
        public long lastCollectedAt;
        public boolean isFavorite;
        public boolean isMastered;    // masteryLevel >= 100
        
        public NotebookEntry(CollectedNote note) {
            this.noteId = note.getNoteId();
            this.word = note.getNoteName();
            this.worldId = note.getWorldId();
            this.rarity = NoteRarity.fromString(note.getNoteType());
            this.masteryLevel = note.getMasteryLevel();
            this.timesCollected = note.getTimesCollected();
            this.perfectHits = note.getPerfectHits();
            this.firstCollectedAt = note.getFirstCollectedAt();
            this.lastCollectedAt = note.getLastCollectedAt();
            this.isFavorite = note.isFavorite();
            this.isMastered = masteryLevel >= 100;
        }
        
        // For testing/preview
        public NotebookEntry(String word, String worldId, NoteRarity rarity) {
            this.noteId = "note_" + word;
            this.word = word;
            this.worldId = worldId;
            this.rarity = rarity;
            this.masteryLevel = 0;
            this.timesCollected = 1;
            this.firstCollectedAt = System.currentTimeMillis();
            this.lastCollectedAt = System.currentTimeMillis();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private List<NotebookEntry> allEntries = new ArrayList<>();
    private List<NotebookEntry> filteredEntries = new ArrayList<>();
    
    private FilterType currentFilter = FilterType.ALL;
    private SortType currentSort = SortType.RECENT_FIRST;
    private String filterValue = null;  // e.g., worldId for BY_WORLD
    private String searchQuery = null;
    
    // Cached statistics
    private NotebookStats cachedStats;
    private boolean statsNeedUpdate = true;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface NotebookListener {
        void onEntriesUpdated(List<NotebookEntry> entries);
        void onStatsUpdated(NotebookStats stats);
        void onMilestoneReached(MilestoneType type, int value);
    }
    
    public enum MilestoneType {
        FIRST_WORD,
        WORDS_10,
        WORDS_50,
        WORDS_100,
        WORDS_500,
        FIRST_LEGENDARY,
        ALL_WORLD_COMPLETE,
        FIRST_MASTERED,
        MASTERED_10
    }
    
    private NotebookListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public MagicNotebookManager() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(NotebookListener listener) {
        this.listener = listener;
    }
    
    /**
     * Load entries from collected notes
     */
    public void loadEntries(List<CollectedNote> notes) {
        allEntries.clear();
        
        for (CollectedNote note : notes) {
            allEntries.add(new NotebookEntry(note));
        }
        
        statsNeedUpdate = true;
        applyFilterAndSort();
    }
    
    /**
     * Add a new entry
     */
    public void addEntry(NotebookEntry entry) {
        // Check if already exists
        for (NotebookEntry existing : allEntries) {
            if (existing.noteId.equals(entry.noteId)) {
                // Update existing
                existing.timesCollected++;
                existing.lastCollectedAt = System.currentTimeMillis();
                statsNeedUpdate = true;
                applyFilterAndSort();
                return;
            }
        }
        
        // Add new
        allEntries.add(entry);
        statsNeedUpdate = true;
        
        // Check milestones
        checkMilestones(entry);
        
        applyFilterAndSort();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 FILTERING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set filter type
     */
    public void setFilter(FilterType filter) {
        setFilter(filter, null);
    }
    
    /**
     * Set filter with value
     */
    public void setFilter(FilterType filter, String value) {
        this.currentFilter = filter;
        this.filterValue = value;
        applyFilterAndSort();
    }
    
    /**
     * Set sort type
     */
    public void setSort(SortType sort) {
        this.currentSort = sort;
        applyFilterAndSort();
    }
    
    /**
     * Set search query
     */
    public void setSearchQuery(String query) {
        this.searchQuery = (query != null && !query.isEmpty()) 
            ? query.toLowerCase().trim() 
            : null;
        applyFilterAndSort();
    }
    
    /**
     * Apply current filter and sort
     */
    private void applyFilterAndSort() {
        filteredEntries.clear();
        
        // Filter
        for (NotebookEntry entry : allEntries) {
            if (matchesFilter(entry) && matchesSearch(entry)) {
                filteredEntries.add(entry);
            }
        }
        
        // Sort
        Collections.sort(filteredEntries, getComparator());
        
        // Notify
        if (listener != null) {
            listener.onEntriesUpdated(filteredEntries);
        }
    }
    
    private boolean matchesFilter(NotebookEntry entry) {
        switch (currentFilter) {
            case ALL:
                return true;
                
            case BY_WORLD:
                return filterValue != null && filterValue.equals(entry.worldId);
                
            case BY_RARITY:
                return filterValue != null && 
                       filterValue.equalsIgnoreCase(entry.rarity.getKey());
                
            case BY_MASTERY:
                if (filterValue == null) return true;
                int minMastery = Integer.parseInt(filterValue);
                return entry.masteryLevel >= minMastery;
                
            case FAVORITES:
                return entry.isFavorite;
                
            case RECENT:
                // Within last 7 days
                long weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                return entry.lastCollectedAt >= weekAgo;
                
            case UNMASTERED:
                return !entry.isMastered;
                
            default:
                return true;
        }
    }
    
    private boolean matchesSearch(NotebookEntry entry) {
        if (searchQuery == null) return true;
        
        return entry.word.toLowerCase().contains(searchQuery) ||
               (entry.meaning != null && entry.meaning.toLowerCase().contains(searchQuery)) ||
               (entry.worldName != null && entry.worldName.toLowerCase().contains(searchQuery));
    }
    
    private Comparator<NotebookEntry> getComparator() {
        switch (currentSort) {
            case ALPHABETICAL:
                return (a, b) -> a.word.compareToIgnoreCase(b.word);
                
            case RECENT_FIRST:
                return (a, b) -> Long.compare(b.lastCollectedAt, a.lastCollectedAt);
                
            case OLDEST_FIRST:
                return (a, b) -> Long.compare(a.firstCollectedAt, b.firstCollectedAt);
                
            case RARITY_HIGH:
                return (a, b) -> Integer.compare(b.rarity.getTier(), a.rarity.getTier());
                
            case RARITY_LOW:
                return (a, b) -> Integer.compare(a.rarity.getTier(), b.rarity.getTier());
                
            case MASTERY_HIGH:
                return (a, b) -> Integer.compare(b.masteryLevel, a.masteryLevel);
                
            case MASTERY_LOW:
                return (a, b) -> Integer.compare(a.masteryLevel, b.masteryLevel);
                
            case WORLD:
                return (a, b) -> {
                    int worldCompare = String.valueOf(a.worldId)
                        .compareTo(String.valueOf(b.worldId));
                    if (worldCompare != 0) return worldCompare;
                    return a.word.compareToIgnoreCase(b.word);
                };
                
            default:
                return (a, b) -> 0;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATISTICS
    // ═══════════════════════════════════════════════════════════════
    
    public static class NotebookStats {
        public int totalWords;
        public int masteredWords;
        public int favoriteWords;
        
        // By rarity
        public int commonCount;
        public int uncommonCount;
        public int rareCount;
        public int epicCount;
        public int legendaryCount;
        
        // By world
        public Map<String, Integer> wordsByWorld = new HashMap<>();
        
        // Performance
        public int totalPerfectHits;
        public float averageMastery;
        
        // Completion
        public float completionPercent;
    }
    
    /**
     * Get notebook statistics
     */
    public NotebookStats getStats() {
        if (statsNeedUpdate || cachedStats == null) {
            cachedStats = calculateStats();
            statsNeedUpdate = false;
        }
        return cachedStats;
    }
    
    private NotebookStats calculateStats() {
        NotebookStats stats = new NotebookStats();
        
        stats.totalWords = allEntries.size();
        
        int totalMastery = 0;
        
        for (NotebookEntry entry : allEntries) {
            // Mastery
            if (entry.isMastered) stats.masteredWords++;
            totalMastery += entry.masteryLevel;
            
            // Favorites
            if (entry.isFavorite) stats.favoriteWords++;
            
            // Rarity
            switch (entry.rarity) {
                case COMMON: stats.commonCount++; break;
                case UNCOMMON: stats.uncommonCount++; break;
                case RARE: stats.rareCount++; break;
                case EPIC: stats.epicCount++; break;
                case LEGENDARY: stats.legendaryCount++; break;
            }
            
            // By world
            int worldCount = stats.wordsByWorld.getOrDefault(entry.worldId, 0);
            stats.wordsByWorld.put(entry.worldId, worldCount + 1);
            
            // Perfect hits
            stats.totalPerfectHits += entry.perfectHits;
        }
        
        // Averages
        stats.averageMastery = stats.totalWords > 0 
            ? (float) totalMastery / stats.totalWords 
            : 0f;
        
        return stats;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏆 MILESTONES
    // ═══════════════════════════════════════════════════════════════
    
    private Set<MilestoneType> achievedMilestones = new HashSet<>();
    
    private void checkMilestones(NotebookEntry newEntry) {
        if (listener == null) return;
        
        int totalWords = allEntries.size();
        
        // First word
        if (totalWords == 1 && !achievedMilestones.contains(MilestoneType.FIRST_WORD)) {
            achievedMilestones.add(MilestoneType.FIRST_WORD);
            listener.onMilestoneReached(MilestoneType.FIRST_WORD, 1);
        }
        
        // Word count milestones
        checkCountMilestone(totalWords, 10, MilestoneType.WORDS_10);
        checkCountMilestone(totalWords, 50, MilestoneType.WORDS_50);
        checkCountMilestone(totalWords, 100, MilestoneType.WORDS_100);
        checkCountMilestone(totalWords, 500, MilestoneType.WORDS_500);
        
        // First legendary
        if (newEntry.rarity == NoteRarity.LEGENDARY && 
            !achievedMilestones.contains(MilestoneType.FIRST_LEGENDARY)) {
            achievedMilestones.add(MilestoneType.FIRST_LEGENDARY);
            listener.onMilestoneReached(MilestoneType.FIRST_LEGENDARY, 1);
        }
    }
    
    private void checkCountMilestone(int count, int target, MilestoneType milestone) {
        if (count >= target && !achievedMilestones.contains(milestone)) {
            achievedMilestones.add(milestone);
            listener.onMilestoneReached(milestone, target);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⭐ FAVORITES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String noteId) {
        for (NotebookEntry entry : allEntries) {
            if (entry.noteId.equals(noteId)) {
                entry.isFavorite = !entry.isFavorite;
                statsNeedUpdate = true;
                applyFilterAndSort();
                break;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public List<NotebookEntry> getFilteredEntries() {
        return new ArrayList<>(filteredEntries);
    }
    
    public List<NotebookEntry> getAllEntries() {
        return new ArrayList<>(allEntries);
    }
    
    public int getTotalCount() {
        return allEntries.size();
    }
    
    public int getFilteredCount() {
        return filteredEntries.size();
    }
    
    public FilterType getCurrentFilter() {
        return currentFilter;
    }
    
    public SortType getCurrentSort() {
        return currentSort;
    }
    
    /**
     * Get entry by ID
     */
    public NotebookEntry getEntry(String noteId) {
        for (NotebookEntry entry : allEntries) {
            if (entry.noteId.equals(noteId)) {
                return entry;
            }
        }
        return null;
    }
    
    /**
     * Get entries by world
     */
    public List<NotebookEntry> getEntriesByWorld(String worldId) {
        List<NotebookEntry> result = new ArrayList<>();
        for (NotebookEntry entry : allEntries) {
            if (worldId.equals(entry.worldId)) {
                result.add(entry);
            }
        }
        return result;
    }
    
    /**
     * Get entries by rarity
     */
    public List<NotebookEntry> getEntriesByRarity(NoteRarity rarity) {
        List<NotebookEntry> result = new ArrayList<>();
        for (NotebookEntry entry : allEntries) {
            if (entry.rarity == rarity) {
                result.add(entry);
            }
        }
        return result;
    }
}
