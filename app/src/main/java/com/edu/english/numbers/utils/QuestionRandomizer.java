package com.edu.english.numbers.utils;

import com.edu.english.numbers.models.NumberQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages question randomization for the Numbers lesson
 * Ensures no repeated questions until all have been asked
 */
public class QuestionRandomizer {
    
    private List<NumberQuestion> questionPool;
    private List<NumberQuestion> currentQueue;
    private int currentIndex;
    
    public QuestionRandomizer(List<NumberQuestion> allQuestions) {
        this.questionPool = new ArrayList<>(allQuestions);
        resetAndShuffle();
    }
    
    /**
     * Reset the queue and shuffle all questions
     */
    public void resetAndShuffle() {
        currentQueue = new ArrayList<>(questionPool);
        Collections.shuffle(currentQueue);
        currentIndex = 0;
    }
    
    /**
     * Get the next question
     * Auto-reshuffles when pool is exhausted
     */
    public NumberQuestion getNextQuestion() {
        if (currentIndex >= currentQueue.size()) {
            // Pool exhausted, reshuffle
            resetAndShuffle();
        }
        
        NumberQuestion question = currentQueue.get(currentIndex);
        currentIndex++;
        return question;
    }
    
    /**
     * Get the current question without advancing
     */
    public NumberQuestion getCurrentQuestion() {
        if (currentIndex > 0 && currentIndex <= currentQueue.size()) {
            return currentQueue.get(currentIndex - 1);
        } else if (currentQueue.size() > 0) {
            return currentQueue.get(0);
        }
        return null;
    }
    
    /**
     * Check if there are more questions in current pool
     */
    public boolean hasMoreQuestions() {
        return currentIndex < currentQueue.size();
    }
    
    /**
     * Get progress: current question number (1-based)
     */
    public int getCurrentQuestionNumber() {
        return currentIndex;
    }
    
    /**
     * Get total questions in pool
     */
    public int getTotalQuestions() {
        return questionPool.size();
    }
    
    /**
     * Get how many questions completed in current cycle
     */
    public int getCompletedInCycle() {
        return currentIndex;
    }
    
    /**
     * Get remaining questions in current cycle
     */
    public int getRemainingInCycle() {
        return currentQueue.size() - currentIndex;
    }
}
