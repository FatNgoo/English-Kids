package com.edu.english.shapes.models;

/**
 * Model class representing an object associated with a shape
 */
public class ShapeObject {
    private String name;
    private String vietnameseName;
    private int imageResId;
    private String audioFileName;
    private ShapeType shapeType;

    public ShapeObject(String name, String vietnameseName, int imageResId, String audioFileName, ShapeType shapeType) {
        this.name = name;
        this.vietnameseName = vietnameseName;
        this.imageResId = imageResId;
        this.audioFileName = audioFileName;
        this.shapeType = shapeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public void setVietnameseName(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }
}
