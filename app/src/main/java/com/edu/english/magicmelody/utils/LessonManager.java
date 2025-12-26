package com.edu.english.magicmelody.utils;

import android.content.Context;
import android.util.Log;

import com.edu.english.magicmelody.data.model.Lesson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * LessonManager - Loads and manages lesson data from JSON assets
 */
public class LessonManager {
    
    private static final String TAG = "LessonManager";
    private static final String LESSONS_DIR = "lessons";
    private static final String LESSON_INDEX_FILE = "lessons/lesson_index.json";
    
    private final Context context;
    private final Gson gson;
    private List<Lesson> cachedLessons;
    
    private static LessonManager instance;
    
    public static LessonManager getInstance(Context context) {
        if (instance == null) {
            instance = new LessonManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private LessonManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }
    
    /**
     * Load all lessons from assets
     */
    public List<Lesson> loadAllLessons() {
        if (cachedLessons != null) {
            return cachedLessons;
        }
        
        cachedLessons = new ArrayList<>();
        
        try {
            // Try to load from index file first
            String indexJson = loadAssetFile(LESSON_INDEX_FILE);
            if (indexJson != null) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> lessonFiles = gson.fromJson(indexJson, listType);
                
                for (String file : lessonFiles) {
                    Lesson lesson = loadLesson(LESSONS_DIR + "/" + file);
                    if (lesson != null) {
                        cachedLessons.add(lesson);
                    }
                }
            } else {
                // Fallback: scan lessons directory
                String[] files = context.getAssets().list(LESSONS_DIR);
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".json") && !file.equals("lesson_index.json")) {
                            Lesson lesson = loadLesson(LESSONS_DIR + "/" + file);
                            if (lesson != null) {
                                cachedLessons.add(lesson);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading lessons", e);
        }
        
        // If no lessons found, create default lessons
        if (cachedLessons.isEmpty()) {
            cachedLessons = createDefaultLessons();
        }
        
        return cachedLessons;
    }
    
    /**
     * Load a specific lesson by ID
     */
    public Lesson getLessonById(String lessonId) {
        List<Lesson> lessons = loadAllLessons();
        for (Lesson lesson : lessons) {
            if (lesson.getLessonId().equals(lessonId)) {
                return lesson;
            }
        }
        return null;
    }
    
    /**
     * Load a lesson from a specific file
     */
    private Lesson loadLesson(String path) {
        String json = loadAssetFile(path);
        if (json != null) {
            try {
                return gson.fromJson(json, Lesson.class);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing lesson: " + path, e);
            }
        }
        return null;
    }
    
    /**
     * Load a file from assets as string
     */
    private String loadAssetFile(String path) {
        try {
            InputStream is = context.getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            Log.d(TAG, "File not found: " + path);
            return null;
        }
    }
    
    /**
     * Get all lesson IDs
     */
    public String[] getAllLessonIds() {
        List<Lesson> lessons = loadAllLessons();
        String[] ids = new String[lessons.size()];
        for (int i = 0; i < lessons.size(); i++) {
            ids[i] = lessons.get(i).getLessonId();
        }
        return ids;
    }
    
    /**
     * Create default lessons when no JSON files found
     */
    private List<Lesson> createDefaultLessons() {
        List<Lesson> lessons = new ArrayList<>();
        
        // Lesson 1: Forest Level
        Lesson lesson1 = createLesson("forest_level_1", "Khu Rừng Thần Tiên", "NATURE");
        lessons.add(lesson1);
        
        // Lesson 2: River Level
        Lesson lesson2 = createLesson("river_level_2", "Dòng Sông Kỳ Diệu", "NATURE");
        lessons.add(lesson2);
        
        // Lesson 3: Mountain Level
        Lesson lesson3 = createLesson("mountain_level_3", "Đỉnh Núi Cao", "NATURE");
        lessons.add(lesson3);
        
        // Lesson 4: Ocean Level
        Lesson lesson4 = createLesson("ocean_level_4", "Biển Xanh Sâu", "NATURE");
        lessons.add(lesson4);
        
        // Lesson 5: Sky Level
        Lesson lesson5 = createLesson("sky_level_5", "Bầu Trời Rộng", "NATURE");
        lessons.add(lesson5);
        
        return lessons;
    }
    
    private Lesson createLesson(String id, String title, String theme) {
        Lesson lesson = new Lesson();
        lesson.setLessonId(id);
        lesson.setTitle(title);
        lesson.setTitleVietnamese(title);
        lesson.setTheme(theme);
        lesson.setMainCharacter("forest_spirit");
        
        // Age configs
        Lesson.AgeConfig toddler = new Lesson.AgeConfig();
        toddler.setBpm(60);
        toddler.setNotesVisible(4);
        toddler.setPerfectWindowMs(220);
        toddler.setGoodWindowMs(380);
        toddler.setGlitchNoteInterval(12);
        lesson.setToddler(toddler);
        
        Lesson.AgeConfig explorer = new Lesson.AgeConfig();
        explorer.setBpm(90);
        explorer.setNotesVisible(6);
        explorer.setPerfectWindowMs(160);
        explorer.setGoodWindowMs(280);
        explorer.setGlitchNoteInterval(8);
        lesson.setExplorer(explorer);
        
        Lesson.AgeConfig master = new Lesson.AgeConfig();
        master.setBpm(120);
        master.setNotesVisible(8);
        master.setPerfectWindowMs(120);
        master.setGoodWindowMs(220);
        master.setGlitchNoteInterval(5);
        lesson.setMaster(master);
        
        // Generate note chart
        List<Lesson.NoteEvent> noteChart = generateNoteChart(id);
        lesson.setNoteChart(noteChart);
        
        // Generate vocabulary
        List<Lesson.VocabItem> vocabulary = generateVocabulary(id);
        lesson.setVocabulary(vocabulary);
        
        return lesson;
    }
    
    private List<Lesson.NoteEvent> generateNoteChart(String lessonId) {
        List<Lesson.NoteEvent> chart = new ArrayList<>();
        
        // Generate a simple pattern of notes
        // Each note is spaced 500ms apart (120 BPM = 500ms per beat)
        long startTime = 2000; // 2 second lead-in
        int noteCount = 20;
        
        String[] vocabKeys = {"tree", "bird", "flower", "sun", "cloud", "rain", "wind"};
        
        for (int i = 0; i < noteCount; i++) {
            Lesson.NoteEvent note = new Lesson.NoteEvent();
            note.setTimeMs(startTime + i * 600); // 600ms between notes
            note.setLane(i % 7); // Cycle through lanes
            note.setVocabKey(vocabKeys[i % vocabKeys.length]);
            note.setBeatIndex(i + 1);
            chart.add(note);
        }
        
        return chart;
    }
    
    private List<Lesson.VocabItem> generateVocabulary(String lessonId) {
        List<Lesson.VocabItem> vocab = new ArrayList<>();
        
        String[][] words = {
            {"tree", "Cây", "0"},
            {"bird", "Chim", "1"},
            {"flower", "Hoa", "2"},
            {"sun", "Mặt trời", "3"},
            {"cloud", "Mây", "4"},
            {"rain", "Mưa", "5"},
            {"wind", "Gió", "6"}
        };
        
        for (String[] word : words) {
            Lesson.VocabItem item = new Lesson.VocabItem();
            item.setKey(word[0]);
            item.setEnglish(word[0].substring(0, 1).toUpperCase() + word[0].substring(1));
            item.setVietnamese(word[1]);
            item.setNoteIndex(Integer.parseInt(word[2]));
            vocab.add(item);
        }
        
        return vocab;
    }
    
    /**
     * Clear cached lessons
     */
    public void clearCache() {
        cachedLessons = null;
    }
}
