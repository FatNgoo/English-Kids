package com.edu.english.model;

/**
 * Model class for AR Animal items
 * Contains all information needed to display an animal in AR/2D mode
 */
public class AnimalArItem {
    
    private String id;          // Unique ID: cat, dog, cow, etc.
    private String nameEn;      // English name: Cat, Dog, etc.
    private String nameVi;      // Vietnamese name: Mèo, Chó, etc.
    private String modelPath;   // Path to .glb model in assets
    private int soundResId;     // Resource ID for animal sound (0 if none)
    private int thumbResId;     // Resource ID for thumbnail drawable
    private float modelScale;   // Scale for AR model (default 0.3-0.5)
    
    public AnimalArItem(String id, String nameEn, String nameVi, 
                        String modelPath, int soundResId, int thumbResId, float modelScale) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
        this.modelPath = modelPath;
        this.soundResId = soundResId;
        this.thumbResId = thumbResId;
        this.modelScale = modelScale;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getNameEn() {
        return nameEn;
    }
    
    public String getNameVi() {
        return nameVi;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    public int getSoundResId() {
        return soundResId;
    }
    
    public int getThumbResId() {
        return thumbResId;
    }
    
    public float getModelScale() {
        return modelScale;
    }
    
    // Check if sound file exists
    public boolean hasSound() {
        return soundResId != 0;
    }
}
