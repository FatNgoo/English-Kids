package com.edu.english.numbers.models;

import java.util.List;

/**
 * Model representing a counting scene with objects to count
 */
public class NumberScene {
    
    public static final int SCENE_APPLES = 1;
    public static final int SCENE_CARS = 2;
    public static final int SCENE_BALLOONS = 3;
    public static final int SCENE_STARS = 4;
    
    private final int sceneId;
    private final String name;
    private final String description;
    private final List<SceneObject> objects;
    
    public NumberScene(int sceneId, String name, String description, List<SceneObject> objects) {
        this.sceneId = sceneId;
        this.name = name;
        this.description = description;
        this.objects = objects;
    }
    
    public int getSceneId() {
        return sceneId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<SceneObject> getObjects() {
        return objects;
    }
    
    /**
     * Object within a scene that can be drawn
     */
    public static class SceneObject {
        
        public enum ObjectType {
            APPLE_RED,
            APPLE_GREEN,
            CAR_RED,
            CAR_BLUE,
            CAR_YELLOW,
            BALLOON_PURPLE,
            BALLOON_PINK,
            STAR_BIG,
            STAR_SMALL
        }
        
        private final ObjectType type;
        private final float x;      // Relative position 0-1
        private final float y;      // Relative position 0-1
        private final float size;   // Relative size 0-1
        private final int color;
        
        public SceneObject(ObjectType type, float x, float y, float size, int color) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
        
        public ObjectType getType() {
            return type;
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public float getSize() {
            return size;
        }
        
        public int getColor() {
            return color;
        }
        
        /**
         * Get the base category of this object (for counting)
         */
        public String getCategory() {
            switch (type) {
                case APPLE_RED:
                case APPLE_GREEN:
                    return "apple";
                case CAR_RED:
                case CAR_BLUE:
                case CAR_YELLOW:
                    return "car";
                case BALLOON_PURPLE:
                case BALLOON_PINK:
                    return "balloon";
                case STAR_BIG:
                case STAR_SMALL:
                    return "star";
                default:
                    return "object";
            }
        }
    }
}
