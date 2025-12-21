package com.edu.english.alphabet_adventure.models;

/**
 * Model representing a word item for the Alphabet Adventure game.
 * Contains the word to spell and optional image/hint.
 */
public class WordItem {
    private String word;
    private String imageEmoji;
    private String hint;

    public WordItem(String word, String imageEmoji) {
        this.word = word.toLowerCase();
        this.imageEmoji = imageEmoji;
        this.hint = null;
    }

    public WordItem(String word, String imageEmoji, String hint) {
        this.word = word.toLowerCase();
        this.imageEmoji = imageEmoji;
        this.hint = hint;
    }

    public String getWord() {
        return word;
    }

    public String getImageEmoji() {
        return imageEmoji;
    }

    public String getHint() {
        return hint;
    }

    public int getWordLength() {
        return word.length();
    }

    public char getCharAt(int index) {
        if (index >= 0 && index < word.length()) {
            return word.charAt(index);
        }
        return ' ';
    }
}
