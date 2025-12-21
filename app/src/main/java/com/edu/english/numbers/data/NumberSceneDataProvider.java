package com.edu.english.numbers.data;

import android.graphics.Color;

import com.edu.english.numbers.models.NumberQuestion;
import com.edu.english.numbers.models.NumberScene;
import com.edu.english.numbers.models.NumberScene.SceneObject;
import com.edu.english.numbers.models.NumberScene.SceneObject.ObjectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data provider for Number scenes and questions
 * Singleton pattern - same as ShapeDataProvider
 * 
 * Contains 4 scenes with 4 questions each = 16 total questions
 */
public class NumberSceneDataProvider {
    
    private static NumberSceneDataProvider instance;
    
    // Colors for objects
    public static final int COLOR_RED = 0xFFE74C3C;
    public static final int COLOR_GREEN = 0xFF27AE60;
    public static final int COLOR_BLUE = 0xFF3498DB;
    public static final int COLOR_YELLOW = 0xFFF1C40F;
    public static final int COLOR_PURPLE = 0xFF9B59B6;
    public static final int COLOR_PINK = 0xFFE91E63;
    public static final int COLOR_GOLD = 0xFFFFD700;
    
    // Background colors for scenes
    public static final int BG_SCENE_APPLES = 0xFFF5E6D3;    // Warm table color
    public static final int BG_SCENE_CARS = 0xFFE8E8E8;      // Gray parking lot
    public static final int BG_SCENE_BALLOONS = 0xFF87CEEB;  // Sky blue
    public static final int BG_SCENE_STARS = 0xFF1A1A2E;     // Night sky
    
    private Map<Integer, NumberScene> scenes;
    private List<NumberQuestion> allQuestions;
    
    // Number word mapping
    private static final Map<Integer, String> NUMBER_WORDS;
    static {
        NUMBER_WORDS = new HashMap<>();
        NUMBER_WORDS.put(1, "one");
        NUMBER_WORDS.put(2, "two");
        NUMBER_WORDS.put(3, "three");
        NUMBER_WORDS.put(4, "four");
        NUMBER_WORDS.put(5, "five");
        NUMBER_WORDS.put(6, "six");
        NUMBER_WORDS.put(7, "seven");
        NUMBER_WORDS.put(8, "eight");
        NUMBER_WORDS.put(9, "nine");
        NUMBER_WORDS.put(10, "ten");
    }
    
    private NumberSceneDataProvider() {
        initializeScenes();
        initializeQuestions();
    }
    
    public static synchronized NumberSceneDataProvider getInstance() {
        if (instance == null) {
            instance = new NumberSceneDataProvider();
        }
        return instance;
    }
    
    /**
     * Get the English word for a number (1-10)
     */
    public static String getNumberWord(int number) {
        return NUMBER_WORDS.getOrDefault(number, String.valueOf(number));
    }
    
    /**
     * Initialize all 4 scenes with their objects
     */
    private void initializeScenes() {
        scenes = new HashMap<>();
        
        // SCENE 1: Table with Apples (5 apples: 3 red, 2 green)
        List<SceneObject> appleObjects = new ArrayList<>();
        // 3 Red apples
        appleObjects.add(new SceneObject(ObjectType.APPLE_RED, 0.15f, 0.4f, 0.12f, COLOR_RED));
        appleObjects.add(new SceneObject(ObjectType.APPLE_RED, 0.45f, 0.35f, 0.13f, COLOR_RED));
        appleObjects.add(new SceneObject(ObjectType.APPLE_RED, 0.75f, 0.42f, 0.11f, COLOR_RED));
        // 2 Green apples
        appleObjects.add(new SceneObject(ObjectType.APPLE_GREEN, 0.30f, 0.55f, 0.12f, COLOR_GREEN));
        appleObjects.add(new SceneObject(ObjectType.APPLE_GREEN, 0.60f, 0.58f, 0.11f, COLOR_GREEN));
        
        scenes.put(NumberScene.SCENE_APPLES, new NumberScene(
                NumberScene.SCENE_APPLES,
                "Table Apples",
                "Apples on the table",
                appleObjects
        ));
        
        // SCENE 2: Parking Cars (6 cars: 4 red, 1 blue, 1 yellow)
        List<SceneObject> carObjects = new ArrayList<>();
        // 4 Red cars
        carObjects.add(new SceneObject(ObjectType.CAR_RED, 0.12f, 0.25f, 0.15f, COLOR_RED));
        carObjects.add(new SceneObject(ObjectType.CAR_RED, 0.38f, 0.25f, 0.15f, COLOR_RED));
        carObjects.add(new SceneObject(ObjectType.CAR_RED, 0.12f, 0.60f, 0.15f, COLOR_RED));
        carObjects.add(new SceneObject(ObjectType.CAR_RED, 0.64f, 0.60f, 0.15f, COLOR_RED));
        // 1 Blue car
        carObjects.add(new SceneObject(ObjectType.CAR_BLUE, 0.64f, 0.25f, 0.15f, COLOR_BLUE));
        // 1 Yellow car
        carObjects.add(new SceneObject(ObjectType.CAR_YELLOW, 0.38f, 0.60f, 0.15f, COLOR_YELLOW));
        
        scenes.put(NumberScene.SCENE_CARS, new NumberScene(
                NumberScene.SCENE_CARS,
                "Parking Cars",
                "Cars in the parking lot",
                carObjects
        ));
        
        // SCENE 3: Balloons (8 balloons: 5 purple, 3 pink)
        List<SceneObject> balloonObjects = new ArrayList<>();
        // 5 Purple balloons
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PURPLE, 0.10f, 0.20f, 0.10f, COLOR_PURPLE));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PURPLE, 0.30f, 0.15f, 0.11f, COLOR_PURPLE));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PURPLE, 0.50f, 0.25f, 0.10f, COLOR_PURPLE));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PURPLE, 0.70f, 0.18f, 0.12f, COLOR_PURPLE));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PURPLE, 0.85f, 0.30f, 0.09f, COLOR_PURPLE));
        // 3 Pink balloons
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PINK, 0.20f, 0.45f, 0.11f, COLOR_PINK));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PINK, 0.55f, 0.50f, 0.10f, COLOR_PINK));
        balloonObjects.add(new SceneObject(ObjectType.BALLOON_PINK, 0.80f, 0.55f, 0.12f, COLOR_PINK));
        
        scenes.put(NumberScene.SCENE_BALLOONS, new NumberScene(
                NumberScene.SCENE_BALLOONS,
                "Balloons",
                "Colorful balloons in the sky",
                balloonObjects
        ));
        
        // SCENE 4: Stars (7 stars: 2 big, 5 small)
        List<SceneObject> starObjects = new ArrayList<>();
        // 2 Big stars
        starObjects.add(new SceneObject(ObjectType.STAR_BIG, 0.25f, 0.30f, 0.18f, COLOR_GOLD));
        starObjects.add(new SceneObject(ObjectType.STAR_BIG, 0.70f, 0.25f, 0.20f, COLOR_GOLD));
        // 5 Small stars
        starObjects.add(new SceneObject(ObjectType.STAR_SMALL, 0.15f, 0.55f, 0.08f, COLOR_GOLD));
        starObjects.add(new SceneObject(ObjectType.STAR_SMALL, 0.40f, 0.50f, 0.07f, COLOR_GOLD));
        starObjects.add(new SceneObject(ObjectType.STAR_SMALL, 0.55f, 0.60f, 0.09f, COLOR_GOLD));
        starObjects.add(new SceneObject(ObjectType.STAR_SMALL, 0.80f, 0.55f, 0.08f, COLOR_GOLD));
        starObjects.add(new SceneObject(ObjectType.STAR_SMALL, 0.50f, 0.35f, 0.06f, COLOR_GOLD));
        
        scenes.put(NumberScene.SCENE_STARS, new NumberScene(
                NumberScene.SCENE_STARS,
                "Stars",
                "Stars in the night sky",
                starObjects
        ));
    }
    
    /**
     * Initialize all 16 questions (4 per scene)
     */
    private void initializeQuestions() {
        allQuestions = new ArrayList<>();
        
        // SCENE 1: Table Apples - 5 apples (3 red, 2 green)
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_APPLES,
                "How many apples are on the table?",
                5, "five", "apples"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_APPLES,
                "How many red apples are on the table?",
                3, "three", "red apples"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_APPLES,
                "How many green apples are on the table?",
                2, "two", "green apples"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_APPLES,
                "How many fruits are on the table?",
                5, "five", "fruits"
        ));
        
        // SCENE 2: Parking Cars - 6 cars (4 red, 1 blue, 1 yellow)
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_CARS,
                "How many cars are in the picture?",
                6, "six", "cars"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_CARS,
                "How many red cars are in the picture?",
                4, "four", "red cars"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_CARS,
                "How many blue cars are in the picture?",
                1, "one", "blue car"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_CARS,
                "How many yellow cars are in the picture?",
                1, "one", "yellow car"
        ));
        
        // SCENE 3: Balloons - 8 balloons (5 purple, 3 pink)
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_BALLOONS,
                "How many balloons are there?",
                8, "eight", "balloons"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_BALLOONS,
                "How many purple balloons are there?",
                5, "five", "purple balloons"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_BALLOONS,
                "How many pink balloons are there?",
                3, "three", "pink balloons"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_BALLOONS,
                "How many balloons are NOT purple?",
                3, "three", "not purple"
        ));
        
        // SCENE 4: Stars - 7 stars (2 big, 5 small)
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_STARS,
                "How many stars are in the sky?",
                7, "seven", "stars"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_STARS,
                "How many big stars are there?",
                2, "two", "big stars"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_STARS,
                "How many small stars are there?",
                5, "five", "small stars"
        ));
        allQuestions.add(new NumberQuestion(
                NumberScene.SCENE_STARS,
                "How many stars are big or small?",
                7, "seven", "all stars"
        ));
    }
    
    /**
     * Get a scene by its ID
     */
    public NumberScene getScene(int sceneId) {
        return scenes.get(sceneId);
    }
    
    /**
     * Get all scenes
     */
    public List<NumberScene> getAllScenes() {
        return new ArrayList<>(scenes.values());
    }
    
    /**
     * Get all questions
     */
    public List<NumberQuestion> getAllQuestions() {
        return new ArrayList<>(allQuestions);
    }
    
    /**
     * Get questions for a specific scene
     */
    public List<NumberQuestion> getQuestionsForScene(int sceneId) {
        List<NumberQuestion> result = new ArrayList<>();
        for (NumberQuestion q : allQuestions) {
            if (q.getSceneId() == sceneId) {
                result.add(q);
            }
        }
        return result;
    }
    
    /**
     * Get a shuffled copy of all questions for gameplay
     */
    public List<NumberQuestion> getShuffledQuestions() {
        List<NumberQuestion> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        return shuffled;
    }
    
    /**
     * Get the background color for a scene
     */
    public int getSceneBackgroundColor(int sceneId) {
        switch (sceneId) {
            case NumberScene.SCENE_APPLES:
                return BG_SCENE_APPLES;
            case NumberScene.SCENE_CARS:
                return BG_SCENE_CARS;
            case NumberScene.SCENE_BALLOONS:
                return BG_SCENE_BALLOONS;
            case NumberScene.SCENE_STARS:
                return BG_SCENE_STARS;
            default:
                return Color.WHITE;
        }
    }
    
    /**
     * Get total question count
     */
    public int getTotalQuestionCount() {
        return allQuestions.size();
    }
}
