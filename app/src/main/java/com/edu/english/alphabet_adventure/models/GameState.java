package com.edu.english.alphabet_adventure.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing the current state of the Alphabet Adventure game.
 */
public class GameState {
    
    public enum Status {
        PLAYING,
        PAUSED,
        WIN,
        LOSE
    }

    private WordItem currentWord;
    private int currentIndex;
    private int lives;
    private int currentLane;
    private List<LetterToken> tokens;
    private Status status;
    private int wordIndex;
    private int score;
    private boolean isMuted;

    public GameState() {
        this.currentIndex = 0;
        this.lives = 3;
        this.currentLane = 2; // Start in middle lane (0-4)
        this.tokens = new ArrayList<>();
        this.status = Status.PLAYING;
        this.wordIndex = 0;
        this.score = 0;
        this.isMuted = false;
    }

    public void reset() {
        this.currentIndex = 0;
        this.lives = 3;
        this.currentLane = 2;
        this.tokens.clear();
        this.status = Status.PLAYING;
    }

    public void nextWord(WordItem word) {
        this.currentWord = word;
        this.currentIndex = 0;
        this.tokens.clear();
        this.status = Status.PLAYING;
        this.wordIndex++;
    }

    // Getters and Setters
    public WordItem getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(WordItem currentWord) {
        this.currentWord = currentWord;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void incrementIndex() {
        this.currentIndex++;
    }

    public int getLives() {
        return lives;
    }

    public void decrementLives() {
        this.lives--;
        if (this.lives <= 0) {
            this.status = Status.LOSE;
        }
    }

    public int getCurrentLane() {
        return currentLane;
    }

    public void setCurrentLane(int lane) {
        if (lane >= 0 && lane < 5) {
            this.currentLane = lane;
        }
    }

    public void moveUp() {
        if (currentLane > 0) {
            currentLane--;
        }
    }

    public void moveDown() {
        if (currentLane < 4) {
            currentLane++;
        }
    }

    public List<LetterToken> getTokens() {
        return tokens;
    }

    public void addToken(LetterToken token) {
        tokens.add(token);
    }

    public void clearTokens() {
        tokens.clear();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isPlaying() {
        return status == Status.PLAYING;
    }

    public boolean isPaused() {
        return status == Status.PAUSED;
    }

    public int getWordIndex() {
        return wordIndex;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void toggleMute() {
        isMuted = !isMuted;
    }

    public char getTargetLetter() {
        if (currentWord != null && currentIndex < currentWord.getWordLength()) {
            return Character.toUpperCase(currentWord.getCharAt(currentIndex));
        }
        return ' ';
    }

    public boolean isWordComplete() {
        return currentWord != null && currentIndex >= currentWord.getWordLength();
    }

    public String getProgressString() {
        if (currentWord == null) return "";
        
        StringBuilder sb = new StringBuilder();
        String word = currentWord.getWord().toUpperCase();
        for (int i = 0; i < word.length(); i++) {
            if (i < currentIndex) {
                sb.append(word.charAt(i));
            } else {
                sb.append("_");
            }
            if (i < word.length() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
