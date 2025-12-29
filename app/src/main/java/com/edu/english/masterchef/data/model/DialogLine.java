package com.edu.english.masterchef.data.model;

/**
 * Represents a dialog line in Scene 1 (ordering)
 */
public class DialogLine {
    private String speaker; // "player", "npc"
    private String text; // English text
    private String translation; // Vietnamese translation
    private String ttsText; // Text for TTS (may differ for pronunciation)
    private int delayMs; // Delay before showing this line (animation timing)
    private boolean needsKaraoke; // Whether to highlight words during TTS

    public DialogLine() {
    }

    public DialogLine(String speaker, String text, String translation, String ttsText, int delayMs, boolean needsKaraoke) {
        this.speaker = speaker;
        this.text = text;
        this.translation = translation;
        this.ttsText = ttsText != null ? ttsText : text;
        this.delayMs = delayMs;
        this.needsKaraoke = needsKaraoke;
    }

    // Simple constructor
    public DialogLine(String speaker, String text, String translation) {
        this(speaker, text, translation, text, 0, true);
    }

    // Getters and Setters
    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTtsText() {
        return ttsText;
    }

    public void setTtsText(String ttsText) {
        this.ttsText = ttsText;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }

    public boolean isNeedsKaraoke() {
        return needsKaraoke;
    }

    public void setNeedsKaraoke(boolean needsKaraoke) {
        this.needsKaraoke = needsKaraoke;
    }
}
