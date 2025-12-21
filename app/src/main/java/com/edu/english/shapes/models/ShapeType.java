package com.edu.english.shapes.models;

/**
 * Enum representing different shape types in the game
 */
public enum ShapeType {
    SQUARE("Square", "Hình vuông", 0xFF4FC3F7),     // Light Blue
    CIRCLE("Circle", "Hình tròn", 0xFFFF8A65),     // Light Orange
    TRIANGLE("Triangle", "Hình tam giác", 0xFFAED581), // Light Green
    RECTANGLE("Rectangle", "Hình chữ nhật", 0xFFBA68C8), // Light Purple
    OVAL("Oval", "Hình bầu dục", 0xFFFFF176),      // Light Yellow
    STAR("Star", "Hình ngôi sao", 0xFFF06292);     // Light Pink

    private final String englishName;
    private final String vietnameseName;
    private final int color;

    ShapeType(String englishName, String vietnameseName, int color) {
        this.englishName = englishName;
        this.vietnameseName = vietnameseName;
        this.color = color;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public int getColor() {
        return color;
    }

    public String getVoiceFileName() {
        return "shape_" + name().toLowerCase();
    }
}
