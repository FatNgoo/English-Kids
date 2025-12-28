package com.edu.english.storybook.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edu.english.storybook.model.Chapter;
import com.edu.english.storybook.model.QuizQuestion;
import com.edu.english.storybook.model.Story;
import com.edu.english.storybook.model.StoryOutline;
import com.edu.english.storybook.model.StorySetup;
import com.edu.english.storybook.model.VocabularyItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Story Generator Service - handles the 3-step story generation process
 */
public class StoryGeneratorService {
    
    private static final String TAG = "StoryGeneratorService";
    
    private final GeminiRestClient geminiClient;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    // Rate limiting
    private long lastGenerateTime = 0;
    private static final long COOLDOWN_MS = 10000; // 10 seconds
    
    public StoryGeneratorService() {
        this.geminiClient = new GeminiRestClient();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public StoryGeneratorService(String apiKey) {
        this.geminiClient = new GeminiRestClient(apiKey);
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Check if generation is allowed (rate limiting)
     */
    public boolean canGenerate() {
        return System.currentTimeMillis() - lastGenerateTime >= COOLDOWN_MS;
    }
    
    /**
     * Get remaining cooldown time in seconds
     */
    public int getCooldownRemaining() {
        long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastGenerateTime);
        return Math.max(0, (int) (remaining / 1000));
    }
    
    /**
     * Generate a complete story using the 3-step process
     */
    public void generateStory(StorySetup setup, StoryGenerationCallback callback) {
        // Check rate limit
        if (!canGenerate()) {
            callback.onError("Please wait " + getCooldownRemaining() + " seconds before generating another story.");
            return;
        }
        
        lastGenerateTime = System.currentTimeMillis();
        
        // Check if API is available
        if (!geminiClient.isApiKeyAvailable()) {
            // Use mock generator for demo
            Log.i(TAG, "API key not available, using mock story generator");
            callback.onProgress("Using demo mode...", 0);
            
            executorService.execute(() -> {
                try {
                    Thread.sleep(1500); // Simulate loading
                    Story mockStory = MockStoryGenerator.generateMockStory(
                        setup.getCategory(), 
                        setup.getAge(), 
                        setup.getReadingLevel()
                    );
                    mainHandler.post(() -> callback.onSuccess(mockStory));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Demo error: " + e.getMessage()));
                }
            });
            return;
        }
        
        // Start the 3-step generation process
        generateStoryWithAI(setup, callback);
    }
    
    private void generateStoryWithAI(StorySetup setup, StoryGenerationCallback callback) {
        callback.onProgress("Step 1/3: Creating story outline...", 0);
        
        // Step 1: Generate outline
        String outlinePrompt = StoryPromptBuilder.buildOutlinePrompt(setup);
        
        geminiClient.generateContent(outlinePrompt, new GeminiRestClient.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    StoryOutline outline = parseOutline(response);
                    if (outline == null || outline.getChapters() == null || outline.getChapters().isEmpty()) {
                        // Retry once
                        retryOutline(setup, callback, outlinePrompt);
                        return;
                    }
                    
                    // Step 2: Generate chapters
                    generateChapters(setup, outline, callback);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Outline parse error", e);
                    retryOutline(setup, callback, outlinePrompt);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Outline generation error: " + error);
                // Fallback to mock
                fallbackToMock(setup, callback);
            }
        });
    }
    
    private void retryOutline(StorySetup setup, StoryGenerationCallback callback, String originalPrompt) {
        callback.onProgress("Retrying outline generation...", 5);
        
        String retryPrompt = StoryPromptBuilder.buildRetryPrompt(originalPrompt);
        geminiClient.generateContent(retryPrompt, new GeminiRestClient.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    StoryOutline outline = parseOutline(response);
                    if (outline != null && outline.getChapters() != null && !outline.getChapters().isEmpty()) {
                        generateChapters(setup, outline, callback);
                    } else {
                        fallbackToMock(setup, callback);
                    }
                } catch (Exception e) {
                    fallbackToMock(setup, callback);
                }
            }
            
            @Override
            public void onError(String error) {
                fallbackToMock(setup, callback);
            }
        });
    }
    
    private void generateChapters(StorySetup setup, StoryOutline outline, StoryGenerationCallback callback) {
        List<Chapter> chapters = new ArrayList<>();
        List<StoryOutline.ChapterOutline> chapterOutlines = outline.getChapters();
        int totalChapters = chapterOutlines.size();
        AtomicInteger completedChapters = new AtomicInteger(0);
        
        callback.onProgress("Step 2/3: Writing chapters... (0/" + totalChapters + ")", 15);
        
        // Generate chapters sequentially
        generateNextChapter(setup, outline, chapterOutlines, chapters, 0, totalChapters, callback);
    }
    
    private void generateNextChapter(StorySetup setup, StoryOutline outline,
                                     List<StoryOutline.ChapterOutline> chapterOutlines,
                                     List<Chapter> chapters, int currentIndex, int total,
                                     StoryGenerationCallback callback) {
        
        if (currentIndex >= chapterOutlines.size()) {
            // All chapters done, move to step 3
            generateVocabAndQuiz(setup, outline, chapters, callback);
            return;
        }
        
        StoryOutline.ChapterOutline chapterOutline = chapterOutlines.get(currentIndex);
        int progress = 15 + (int) ((currentIndex / (float) total) * 60);
        callback.onProgress("Step 2/3: Writing chapter " + (currentIndex + 1) + "/" + total + "...", progress);
        
        String chapterPrompt = StoryPromptBuilder.buildChapterPrompt(
            setup, outline.getTitle(), 
            chapterOutline.getHeading(), chapterOutline.getSummary(),
            currentIndex + 1, total
        );
        
        geminiClient.generateContent(chapterPrompt, new GeminiRestClient.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Chapter chapter = parseChapter(response);
                    if (chapter != null) {
                        chapters.add(chapter);
                    } else {
                        // Use outline as fallback
                        chapters.add(new Chapter(
                            chapterOutline.getHeading(),
                            chapterOutline.getSummary(),
                            "Chapter content is being prepared..."
                        ));
                    }
                    
                    // Generate next chapter
                    generateNextChapter(setup, outline, chapterOutlines, chapters, 
                                       currentIndex + 1, total, callback);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Chapter parse error", e);
                    // Add placeholder and continue
                    chapters.add(new Chapter(
                        chapterOutline.getHeading(),
                        chapterOutline.getSummary(),
                        "Chapter content is being prepared..."
                    ));
                    generateNextChapter(setup, outline, chapterOutlines, chapters,
                                       currentIndex + 1, total, callback);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Chapter generation error: " + error);
                // Add placeholder and continue
                chapters.add(new Chapter(
                    chapterOutline.getHeading(),
                    chapterOutline.getSummary(),
                    "Chapter content is being prepared..."
                ));
                generateNextChapter(setup, outline, chapterOutlines, chapters,
                                   currentIndex + 1, total, callback);
            }
        });
    }
    
    private void generateVocabAndQuiz(StorySetup setup, StoryOutline outline, 
                                      List<Chapter> chapters, StoryGenerationCallback callback) {
        callback.onProgress("Step 3/3: Creating vocabulary and quiz...", 80);
        
        String vocabPrompt = StoryPromptBuilder.buildVocabQuizPrompt(
            setup, outline.getTitle(),
            outline.getVocabSeed(),
            outline.getQuestionTopics()
        );
        
        geminiClient.generateContent(vocabPrompt, new GeminiRestClient.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    VocabQuizResult result = parseVocabQuiz(response);
                    
                    // Build final story
                    Story story = new Story();
                    story.setId(UUID.randomUUID().toString());
                    story.setTitle(outline.getTitle());
                    story.setCategory(setup.getCategory());
                    story.setAge(setup.getAge());
                    story.setReadingLevel(setup.getReadingLevel());
                    story.setChapters(chapters);
                    story.setVocabulary(result != null ? result.vocabulary : new ArrayList<>());
                    story.setQuestions(result != null ? result.questions : new ArrayList<>());
                    
                    callback.onProgress("Complete!", 100);
                    callback.onSuccess(story);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Vocab/Quiz parse error", e);
                    // Return story without vocab/quiz
                    Story story = new Story();
                    story.setId(UUID.randomUUID().toString());
                    story.setTitle(outline.getTitle());
                    story.setCategory(setup.getCategory());
                    story.setAge(setup.getAge());
                    story.setReadingLevel(setup.getReadingLevel());
                    story.setChapters(chapters);
                    story.setVocabulary(new ArrayList<>());
                    story.setQuestions(new ArrayList<>());
                    
                    callback.onSuccess(story);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Vocab/Quiz generation error: " + error);
                // Return story without vocab/quiz
                Story story = new Story();
                story.setId(UUID.randomUUID().toString());
                story.setTitle(outline.getTitle());
                story.setCategory(setup.getCategory());
                story.setAge(setup.getAge());
                story.setReadingLevel(setup.getReadingLevel());
                story.setChapters(chapters);
                story.setVocabulary(new ArrayList<>());
                story.setQuestions(new ArrayList<>());
                
                callback.onSuccess(story);
            }
        });
    }
    
    private void fallbackToMock(StorySetup setup, StoryGenerationCallback callback) {
        callback.onProgress("Using demo content...", 50);
        
        executorService.execute(() -> {
            try {
                Thread.sleep(500);
                Story mockStory = MockStoryGenerator.generateMockStory(
                    setup.getCategory(),
                    setup.getAge(),
                    setup.getReadingLevel()
                );
                mainHandler.post(() -> {
                    callback.onProgress("Complete!", 100);
                    callback.onSuccess(mockStory);
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    // Parsing methods
    private StoryOutline parseOutline(String json) {
        try {
            return gson.fromJson(json, StoryOutline.class);
        } catch (Exception e) {
            Log.e(TAG, "Outline parse failed: " + e.getMessage());
            return null;
        }
    }
    
    private Chapter parseChapter(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String heading = obj.has("heading") ? obj.get("heading").getAsString() : "";
            String content = obj.has("content") ? obj.get("content").getAsString() : "";
            return new Chapter(heading, content);
        } catch (Exception e) {
            Log.e(TAG, "Chapter parse failed: " + e.getMessage());
            return null;
        }
    }
    
    private VocabQuizResult parseVocabQuiz(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            VocabQuizResult result = new VocabQuizResult();
            
            // Parse vocabulary
            if (obj.has("vocabulary")) {
                JsonArray vocabArray = obj.getAsJsonArray("vocabulary");
                result.vocabulary = new ArrayList<>();
                for (int i = 0; i < vocabArray.size(); i++) {
                    JsonObject v = vocabArray.get(i).getAsJsonObject();
                    VocabularyItem item = new VocabularyItem(
                        v.has("word") ? v.get("word").getAsString() : "",
                        v.has("meaning_vi") ? v.get("meaning_vi").getAsString() : "",
                        v.has("exampleSentence") ? v.get("exampleSentence").getAsString() : ""
                    );
                    result.vocabulary.add(item);
                }
            }
            
            // Parse questions
            if (obj.has("questions")) {
                JsonArray qArray = obj.getAsJsonArray("questions");
                result.questions = new ArrayList<>();
                for (int i = 0; i < qArray.size(); i++) {
                    JsonObject q = qArray.get(i).getAsJsonObject();
                    List<String> choices = new ArrayList<>();
                    if (q.has("choices")) {
                        JsonArray choicesArray = q.getAsJsonArray("choices");
                        for (int j = 0; j < choicesArray.size(); j++) {
                            choices.add(choicesArray.get(j).getAsString());
                        }
                    }
                    QuizQuestion question = new QuizQuestion(
                        q.has("q") ? q.get("q").getAsString() : "",
                        choices,
                        q.has("answerIndex") ? q.get("answerIndex").getAsInt() : 0
                    );
                    result.questions.add(question);
                }
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Vocab/Quiz parse failed: " + e.getMessage());
            return null;
        }
    }
    
    private static class VocabQuizResult {
        List<VocabularyItem> vocabulary;
        List<QuizQuestion> questions;
    }
    
    /**
     * Callback interface for story generation (simple)
     */
    public interface StoryGenerationCallback {
        void onProgress(String step, int percentage);
        void onSuccess(Story story);
        void onError(String error);
    }
    
    /**
     * Detailed callback interface for story generation (used by UI)
     */
    public interface StoryCallback {
        void onOutlineGenerated();
        void onChapterGenerated(int chapterIndex, int totalChapters);
        void onVocabQuizGenerated();
        void onSuccess(Story story);
        void onError(String error);
    }
    
    /**
     * Generate story with detailed callbacks for UI progress
     */
    public void generateStory(StorySetup setup, StoryCallback callback) {
        // Adapt the StoryCallback to StoryGenerationCallback
        generateStory(setup, new StoryGenerationCallback() {
            private int lastChapterIndex = -1;
            private int totalChapters = 5; // Default
            private boolean outlineReported = false;
            
            @Override
            public void onProgress(String step, int percentage) {
                // Parse progress messages to call appropriate StoryCallback methods
                if (step.contains("outline") && !outlineReported) {
                    if (percentage > 10) {
                        outlineReported = true;
                        callback.onOutlineGenerated();
                    }
                } else if (step.contains("chapter") || step.contains("Chapter")) {
                    // Extract chapter info: "Writing chapter 2/5"
                    try {
                        if (step.contains("/")) {
                            String[] parts = step.split("/");
                            for (int i = 0; i < parts.length; i++) {
                                String part = parts[i];
                                // Find the number before the /
                                String numStr = part.replaceAll("[^0-9]", "");
                                if (!numStr.isEmpty() && i == 0) {
                                    int currentChapter = Integer.parseInt(numStr.substring(numStr.length() - 1)) - 1;
                                    if (parts.length > 1) {
                                        String totalStr = parts[1].replaceAll("[^0-9]", "");
                                        if (!totalStr.isEmpty()) {
                                            totalChapters = Integer.parseInt(totalStr.substring(0, 1));
                                        }
                                    }
                                    if (currentChapter != lastChapterIndex && currentChapter >= 0) {
                                        lastChapterIndex = currentChapter;
                                        callback.onChapterGenerated(currentChapter, totalChapters);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                } else if (step.contains("vocab") || step.contains("quiz") || step.contains("Step 3")) {
                    callback.onVocabQuizGenerated();
                }
            }
            
            @Override
            public void onSuccess(Story story) {
                callback.onSuccess(story);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}
