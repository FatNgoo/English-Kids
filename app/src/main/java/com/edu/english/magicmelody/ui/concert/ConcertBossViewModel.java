package com.edu.english.magicmelody.ui.concert;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.model.Lesson;
import com.edu.english.magicmelody.utils.LessonManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for ConcertBossActivity
 */
public class ConcertBossViewModel extends AndroidViewModel {
    
    public enum GameState {
        IDLE,
        PLAYING,
        WON,
        LOST
    }
    
    private final LessonManager lessonManager;
    
    private final MutableLiveData<Integer> bossHpLiveData = new MutableLiveData<>(100);
    private final MutableLiveData<Integer> scoreLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<String> currentVocabLiveData = new MutableLiveData<>();
    private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>(GameState.IDLE);
    private final MutableLiveData<Integer> micLevelLiveData = new MutableLiveData<>(0);
    
    private int maxBossHp = 100;
    private String[] vocabList;
    private String currentTheme = "NATURE";
    private int lessonId = 1;
    
    public ConcertBossViewModel(@NonNull Application application) {
        super(application);
        lessonManager = LessonManager.getInstance(application);
    }
    
    public void loadLesson(int lessonId, String theme) {
        this.lessonId = lessonId;
        this.currentTheme = theme;
        
        Lesson lesson = lessonManager.getLessonById(String.valueOf(lessonId));
        if (lesson != null && lesson.getVocabulary() != null) {
            List<String> vocabs = new ArrayList<>();
            for (Lesson.VocabItem item : lesson.getVocabulary()) {
                vocabs.add(item.getEnglish());
            }
            vocabList = vocabs.toArray(new String[0]);
        } else {
            // Default vocab
            vocabList = new String[]{"apple", "banana", "cat", "dog", "elephant"};
        }
    }
    
    public void startGame() {
        bossHpLiveData.setValue(maxBossHp);
        scoreLiveData.setValue(0);
        gameStateLiveData.setValue(GameState.PLAYING);
        
        if (vocabList != null && vocabList.length > 0) {
            currentVocabLiveData.setValue(vocabList[0]);
        }
    }
    
    public void resetGame() {
        startGame();
    }
    
    public void dealDamage(int damage) {
        Integer currentHp = bossHpLiveData.getValue();
        if (currentHp == null) currentHp = maxBossHp;
        
        int newHp = Math.max(0, currentHp - damage);
        bossHpLiveData.setValue(newHp);
        
        // Add score
        Integer currentScore = scoreLiveData.getValue();
        if (currentScore == null) currentScore = 0;
        scoreLiveData.setValue(currentScore + damage * 10);
        
        // Check win condition
        if (newHp <= 0) {
            gameStateLiveData.setValue(GameState.WON);
        }
    }
    
    public void setCurrentVocab(String vocab) {
        currentVocabLiveData.setValue(vocab);
    }
    
    public void setMicLevel(int level) {
        micLevelLiveData.setValue(level);
    }
    
    public LiveData<Integer> getBossHp() {
        return bossHpLiveData;
    }
    
    public LiveData<Integer> getScore() {
        return scoreLiveData;
    }
    
    public LiveData<String> getCurrentVocab() {
        return currentVocabLiveData;
    }
    
    public LiveData<GameState> getGameState() {
        return gameStateLiveData;
    }
    
    public LiveData<Integer> getMicLevel() {
        return micLevelLiveData;
    }
    
    public String[] getVocabList() {
        return vocabList;
    }
    
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    public int getLessonId() {
        return lessonId;
    }
}
