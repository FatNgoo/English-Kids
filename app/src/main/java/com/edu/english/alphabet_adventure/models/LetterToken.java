package com.edu.english.alphabet_adventure.models;

/**
 * Model representing a letter token moving on a lane.
 */
public class LetterToken {
    private char letter;
    private int lane;
    private float positionX;
    private boolean isCorrect;
    private boolean isActive;

    public LetterToken(char letter, int lane, float startX, boolean isCorrect) {
        this.letter = Character.toUpperCase(letter);
        this.lane = lane;
        this.positionX = startX;
        this.isCorrect = isCorrect;
        this.isActive = true;
    }

    public char getLetter() {
        return letter;
    }

    public int getLane() {
        return lane;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void moveLeft(float delta) {
        this.positionX -= delta;
    }
}
