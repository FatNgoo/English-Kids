package com.edu.english.data;

import com.edu.english.R;
import com.edu.english.model.AnimalArItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for AR Animal data
 * Contains mock data for 6 animals as specified
 * 
 * NOTE: Sound files (res/raw/*.mp3) need to be added manually.
 * Until then, soundResId is set to 0 (no sound).
 * After adding MP3 files, update the soundResId values below.
 * 
 * Example: When you add cat.mp3, change 0 to R.raw.cat
 */
public class AnimalArRepository {
    
    private static AnimalArRepository instance;
    private List<AnimalArItem> animals;
    
    private AnimalArRepository() {
        initAnimals();
    }
    
    public static synchronized AnimalArRepository getInstance() {
        if (instance == null) {
            instance = new AnimalArRepository();
        }
        return instance;
    }
    
    private void initAnimals() {
        animals = new ArrayList<>();
        
        // NOTE: Sound resource IDs are set to 0 (no sound) as placeholders.
        // After adding MP3 files to res/raw/, update these to R.raw.cat, etc.
        
        // (1) Cat - using new image an_cat.png
        animals.add(new AnimalArItem(
            "cat",
            "Cat",
            "Mèo",
            "models/cat.glb",
            0, // TODO: Change to R.raw.cat after adding cat.mp3
            R.drawable.an_cat,
            0.4f
        ));
        
        // (2) Dog - using new image an_dog.png
        animals.add(new AnimalArItem(
            "dog",
            "Dog",
            "Chó",
            "models/dog.glb",
            0, // TODO: Change to R.raw.dog after adding dog.mp3
            R.drawable.an_dog,
            0.4f
        ));
        
        // (3) Cow - using new image an_cow.png
        animals.add(new AnimalArItem(
            "cow",
            "Cow",
            "Bò",
            "models/cow.glb",
            0, // TODO: Change to R.raw.cow after adding cow.mp3
            R.drawable.an_cow,
            0.5f
        ));
        
        // (4) Lion - using new image an_lion.png
        animals.add(new AnimalArItem(
            "lion",
            "Lion",
            "Sư tử",
            "models/lion.glb",
            0, // TODO: Change to R.raw.lion after adding lion.mp3
            R.drawable.an_lion,
            0.45f
        ));
        
        // (5) Monkey - using new image an_monkey.png
        animals.add(new AnimalArItem(
            "monkey",
            "Monkey",
            "Khỉ",
            "models/monkey.glb",
            0, // TODO: Change to R.raw.monkey after adding monkey.mp3
            R.drawable.an_monkey,
            0.35f
        ));
        
        // (6) Elephant - using new image an_elephant.png
        animals.add(new AnimalArItem(
            "elephant",
            "Elephant",
            "Voi",
            "models/elephant.glb",
            0, // TODO: Change to R.raw.elephant after adding elephant.mp3
            R.drawable.an_elephant,
            0.5f
        ));
    }
    
    /**
     * Get all animals
     */
    public List<AnimalArItem> getAllAnimals() {
        return new ArrayList<>(animals);
    }
    
    /**
     * Find animal by ID
     * @param id Animal ID (cat, dog, etc.)
     * @return AnimalArItem or null if not found
     */
    public AnimalArItem getAnimalById(String id) {
        for (AnimalArItem animal : animals) {
            if (animal.getId().equals(id)) {
                return animal;
            }
        }
        return null;
    }
}
