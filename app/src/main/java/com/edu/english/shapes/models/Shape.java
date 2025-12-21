package com.edu.english.shapes.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a complete shape with its associated objects
 */
public class Shape {
    private ShapeType type;
    private List<ShapeObject> objects;
    private boolean completed;
    private int tracingProgress;

    public Shape(ShapeType type) {
        this.type = type;
        this.objects = new ArrayList<>();
        this.completed = false;
        this.tracingProgress = 0;
    }

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }

    public List<ShapeObject> getObjects() {
        return objects;
    }

    public void setObjects(List<ShapeObject> objects) {
        this.objects = objects;
    }

    public void addObject(ShapeObject object) {
        this.objects.add(object);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getTracingProgress() {
        return tracingProgress;
    }

    public void setTracingProgress(int tracingProgress) {
        this.tracingProgress = tracingProgress;
    }

    public String getName() {
        return type.getEnglishName();
    }

    public int getColor() {
        return type.getColor();
    }
}
