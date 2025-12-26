package com.edu.english.magicmelody.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.local.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.local.MistakeDao;
import com.edu.english.magicmelody.data.model.MistakeEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Mistake data
 * Handles tracking mistakes for Glitch Note intelligent review
 */
public class MistakeRepository {
    
    private final MistakeDao mistakeDao;
    private final ExecutorService executor;
    
    public MistakeRepository(Context context) {
        MagicMelodyDatabase db = MagicMelodyDatabase.getInstance(context);
        mistakeDao = db.mistakeDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public void incrementWrong(String vocab, String lessonId) {
        executor.execute(() -> mistakeDao.incrementWrong(vocab, lessonId));
    }
    
    public List<MistakeEntity> topMistakes(int limit) {
        return mistakeDao.topMistakes(limit);
    }
    
    public LiveData<List<MistakeEntity>> getAllMistakes() {
        return mistakeDao.getAllMistakes();
    }
    
    public List<MistakeEntity> getMistakesByLesson(String lessonId) {
        return mistakeDao.getMistakesByLesson(lessonId);
    }
    
    public void delete(String vocab) {
        executor.execute(() -> mistakeDao.delete(vocab));
    }
    
    public void deleteAll() {
        executor.execute(mistakeDao::deleteAll);
    }
    
    /**
     * Get random vocab from top mistakes for Glitch Note
     * Returns null if no mistakes recorded
     */
    public String getRandomGlitchVocab(int maxToConsider) {
        List<MistakeEntity> top = mistakeDao.topMistakes(maxToConsider);
        if (top == null || top.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * top.size());
        return top.get(randomIndex).getVocab();
    }
}
