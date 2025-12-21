package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * ImageDisplay - Displays image/emoji corresponding to the current flashcard
 * Shows in the center of the screen above flashcard carousel
 */
public class ImageDisplay {
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Display area
    private float displayWidth;
    private float displayHeight;
    private float displayY;
    private float displayX;
    
    // Current word and letter info
    private String currentWord = "";
    private char currentLetter = 'A';
    private int letterColor = 0xFF6C63FF;
    
    // Animation
    private float visibilityProgress = 0f;
    private float targetVisibility = 0f;
    private boolean isVisible = false;
    
    // Scale animation when word changes
    private float scaleProgress = 1f;
    private float targetScale = 1f;
    
    // Paints
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint emojiPaint;
    private Paint wordPaint;
    private Paint shadowPaint;
    
    // Emoji mapping for common words
    private static final Map<String, String> WORD_EMOJIS = new HashMap<>();
    
    static {
        // A words
        WORD_EMOJIS.put("Apple", "🍎");
        WORD_EMOJIS.put("Ant", "🐜");
        WORD_EMOJIS.put("Airplane", "✈️");
        WORD_EMOJIS.put("Alligator", "🐊");
        WORD_EMOJIS.put("Astronaut", "👨‍🚀");
        WORD_EMOJIS.put("Avocado", "🥑");
        
        // B words
        WORD_EMOJIS.put("Ball", "⚽");
        WORD_EMOJIS.put("Bear", "🐻");
        WORD_EMOJIS.put("Banana", "🍌");
        WORD_EMOJIS.put("Butterfly", "🦋");
        WORD_EMOJIS.put("Boat", "⛵");
        WORD_EMOJIS.put("Bird", "🐦");
        
        // C words
        WORD_EMOJIS.put("Cat", "🐱");
        WORD_EMOJIS.put("Car", "🚗");
        WORD_EMOJIS.put("Cake", "🎂");
        WORD_EMOJIS.put("Cow", "🐄");
        WORD_EMOJIS.put("Carrot", "🥕");
        WORD_EMOJIS.put("Cloud", "☁️");
        
        // D words
        WORD_EMOJIS.put("Dog", "🐕");
        WORD_EMOJIS.put("Duck", "🦆");
        WORD_EMOJIS.put("Donut", "🍩");
        WORD_EMOJIS.put("Dinosaur", "🦕");
        WORD_EMOJIS.put("Drum", "🥁");
        WORD_EMOJIS.put("Dolphin", "🐬");
        
        // E words
        WORD_EMOJIS.put("Elephant", "🐘");
        WORD_EMOJIS.put("Egg", "🥚");
        WORD_EMOJIS.put("Eagle", "🦅");
        WORD_EMOJIS.put("Earth", "🌍");
        WORD_EMOJIS.put("Ear", "👂");
        WORD_EMOJIS.put("Eye", "👁️");
        
        // F words
        WORD_EMOJIS.put("Fish", "🐟");
        WORD_EMOJIS.put("Frog", "🐸");
        WORD_EMOJIS.put("Flower", "🌸");
        WORD_EMOJIS.put("Fox", "🦊");
        WORD_EMOJIS.put("Fire", "🔥");
        WORD_EMOJIS.put("Fan", "🌀");
        
        // G words
        WORD_EMOJIS.put("Giraffe", "🦒");
        WORD_EMOJIS.put("Grape", "🍇");
        WORD_EMOJIS.put("Guitar", "🎸");
        WORD_EMOJIS.put("Goat", "🐐");
        WORD_EMOJIS.put("Gift", "🎁");
        WORD_EMOJIS.put("Garden", "🌻");
        
        // H words
        WORD_EMOJIS.put("House", "🏠");
        WORD_EMOJIS.put("Horse", "🐴");
        WORD_EMOJIS.put("Heart", "❤️");
        WORD_EMOJIS.put("Hat", "🎩");
        WORD_EMOJIS.put("Honey", "🍯");
        WORD_EMOJIS.put("Hamburger", "🍔");
        
        // I words
        WORD_EMOJIS.put("Ice cream", "🍦");
        WORD_EMOJIS.put("Igloo", "🏠");
        WORD_EMOJIS.put("Island", "🏝️");
        WORD_EMOJIS.put("Insect", "🐛");
        WORD_EMOJIS.put("Iron", "🔧");
        WORD_EMOJIS.put("Iris", "🌺");
        
        // J words
        WORD_EMOJIS.put("Jellyfish", "🎐");
        WORD_EMOJIS.put("Juice", "🧃");
        WORD_EMOJIS.put("Jacket", "🧥");
        WORD_EMOJIS.put("Jam", "🍓");
        WORD_EMOJIS.put("Jet", "🛩️");
        WORD_EMOJIS.put("Jungle", "🌴");
        
        // K words
        WORD_EMOJIS.put("Kite", "🪁");
        WORD_EMOJIS.put("Kangaroo", "🦘");
        WORD_EMOJIS.put("Key", "🔑");
        WORD_EMOJIS.put("King", "👑");
        WORD_EMOJIS.put("Koala", "🐨");
        WORD_EMOJIS.put("Kitchen", "🍳");
        
        // L words
        WORD_EMOJIS.put("Lion", "🦁");
        WORD_EMOJIS.put("Lemon", "🍋");
        WORD_EMOJIS.put("Lamp", "💡");
        WORD_EMOJIS.put("Leaf", "🍃");
        WORD_EMOJIS.put("Ladybug", "🐞");
        WORD_EMOJIS.put("Lizard", "🦎");
        
        // M words
        WORD_EMOJIS.put("Monkey", "🐵");
        WORD_EMOJIS.put("Moon", "🌙");
        WORD_EMOJIS.put("Mango", "🥭");
        WORD_EMOJIS.put("Mouse", "🐭");
        WORD_EMOJIS.put("Music", "🎵");
        WORD_EMOJIS.put("Mountain", "⛰️");
        
        // N words
        WORD_EMOJIS.put("Nest", "🪹");
        WORD_EMOJIS.put("Nut", "🥜");
        WORD_EMOJIS.put("Nose", "👃");
        WORD_EMOJIS.put("Noodle", "🍜");
        WORD_EMOJIS.put("Nurse", "👩‍⚕️");
        WORD_EMOJIS.put("Notebook", "📓");
        
        // O words
        WORD_EMOJIS.put("Orange", "🍊");
        WORD_EMOJIS.put("Owl", "🦉");
        WORD_EMOJIS.put("Ocean", "🌊");
        WORD_EMOJIS.put("Octopus", "🐙");
        WORD_EMOJIS.put("Onion", "🧅");
        WORD_EMOJIS.put("Oven", "🔲");
        
        // P words
        WORD_EMOJIS.put("Pig", "🐷");
        WORD_EMOJIS.put("Pizza", "🍕");
        WORD_EMOJIS.put("Penguin", "🐧");
        WORD_EMOJIS.put("Panda", "🐼");
        WORD_EMOJIS.put("Piano", "🎹");
        WORD_EMOJIS.put("Pumpkin", "🎃");
        
        // Q words
        WORD_EMOJIS.put("Queen", "👸");
        WORD_EMOJIS.put("Quilt", "🛏️");
        WORD_EMOJIS.put("Question", "❓");
        WORD_EMOJIS.put("Quail", "🐦");
        WORD_EMOJIS.put("Quarter", "🪙");
        WORD_EMOJIS.put("Quiet", "🤫");
        
        // R words
        WORD_EMOJIS.put("Rabbit", "🐰");
        WORD_EMOJIS.put("Rainbow", "🌈");
        WORD_EMOJIS.put("Robot", "🤖");
        WORD_EMOJIS.put("Rocket", "🚀");
        WORD_EMOJIS.put("Rose", "🌹");
        WORD_EMOJIS.put("Rain", "🌧️");
        
        // S words
        WORD_EMOJIS.put("Sun", "☀️");
        WORD_EMOJIS.put("Star", "⭐");
        WORD_EMOJIS.put("Snake", "🐍");
        WORD_EMOJIS.put("Strawberry", "🍓");
        WORD_EMOJIS.put("Ship", "🚢");
        WORD_EMOJIS.put("Smile", "😊");
        
        // T words
        WORD_EMOJIS.put("Tiger", "🐯");
        WORD_EMOJIS.put("Tree", "🌳");
        WORD_EMOJIS.put("Train", "🚂");
        WORD_EMOJIS.put("Turtle", "🐢");
        WORD_EMOJIS.put("Tomato", "🍅");
        WORD_EMOJIS.put("Trumpet", "🎺");
        
        // U words
        WORD_EMOJIS.put("Umbrella", "☂️");
        WORD_EMOJIS.put("Unicorn", "🦄");
        WORD_EMOJIS.put("Up", "⬆️");
        WORD_EMOJIS.put("Under", "⬇️");
        WORD_EMOJIS.put("Uniform", "👔");
        WORD_EMOJIS.put("UFO", "🛸");
        
        // V words
        WORD_EMOJIS.put("Violin", "🎻");
        WORD_EMOJIS.put("Vegetable", "🥬");
        WORD_EMOJIS.put("Van", "🚐");
        WORD_EMOJIS.put("Volcano", "🌋");
        WORD_EMOJIS.put("Vase", "🏺");
        WORD_EMOJIS.put("Valentine", "💝");
        
        // W words
        WORD_EMOJIS.put("Water", "💧");
        WORD_EMOJIS.put("Whale", "🐋");
        WORD_EMOJIS.put("Watermelon", "🍉");
        WORD_EMOJIS.put("Wolf", "🐺");
        WORD_EMOJIS.put("Window", "🪟");
        WORD_EMOJIS.put("Watch", "⌚");
        
        // X words
        WORD_EMOJIS.put("Xylophone", "🎵");
        WORD_EMOJIS.put("X-ray", "🩻");
        WORD_EMOJIS.put("Box", "📦");
        WORD_EMOJIS.put("Six", "6️⃣");
        WORD_EMOJIS.put("Mix", "🥣");
        
        // Y words
        WORD_EMOJIS.put("Yogurt", "🥛");
        WORD_EMOJIS.put("Yacht", "🛥️");
        WORD_EMOJIS.put("Yellow", "💛");
        WORD_EMOJIS.put("Yak", "🐃");
        WORD_EMOJIS.put("Yarn", "🧶");
        WORD_EMOJIS.put("Yo-yo", "🪀");
        
        // Z words
        WORD_EMOJIS.put("Zebra", "🦓");
        WORD_EMOJIS.put("Zoo", "🦁");
        WORD_EMOJIS.put("Zero", "0️⃣");
        WORD_EMOJIS.put("Zipper", "🔗");
        WORD_EMOJIS.put("Zucchini", "🥒");
        WORD_EMOJIS.put("Zigzag", "〰️");
    }
    
    public ImageDisplay(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        calculateDimensions();
        initPaints();
    }
    
    private void calculateDimensions() {
        // Display area - centered, above flashcards
        displayWidth = screenWidth * 0.7f;
        displayHeight = displayWidth * 0.8f;
        displayX = (screenWidth - displayWidth) / 2f;
        displayY = screenHeight * 0.18f;
    }
    
    private void initPaints() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);
        
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(50, 0, 0, 0));
        
        emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        wordPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        wordPaint.setColor(0xFF333333);
    }
    
    /**
     * Set the current word to display
     */
    public void setWord(String word, char letter, int letterColor) {
        if (!word.equals(currentWord)) {
            this.currentWord = word;
            this.currentLetter = letter;
            this.letterColor = letterColor;
            
            // Trigger scale animation
            scaleProgress = 0.8f;
            targetScale = 1f;
        }
    }
    
    /**
     * Show the image display
     */
    public void show() {
        targetVisibility = 1f;
        isVisible = true;
    }
    
    /**
     * Hide the image display
     */
    public void hide() {
        targetVisibility = 0f;
        currentWord = "";
    }
    
    /**
     * Update animations
     */
    public void update(float deltaTime) {
        // Update visibility
        if (visibilityProgress < targetVisibility) {
            visibilityProgress = Math.min(targetVisibility, visibilityProgress + deltaTime * 4f);
        } else if (visibilityProgress > targetVisibility) {
            visibilityProgress = Math.max(targetVisibility, visibilityProgress - deltaTime * 4f);
            if (visibilityProgress <= 0) {
                isVisible = false;
            }
        }
        
        // Update scale
        if (scaleProgress < targetScale) {
            scaleProgress = Math.min(targetScale, scaleProgress + deltaTime * 5f);
        }
    }
    
    /**
     * Draw the image display
     */
    public void draw(Canvas canvas) {
        if (!isVisible || visibilityProgress <= 0 || currentWord.isEmpty()) return;
        
        float alpha = easeOutCubic(visibilityProgress);
        float scale = easeOutBack(scaleProgress);
        
        // Calculate scaled dimensions
        float scaledWidth = displayWidth * scale;
        float scaledHeight = displayHeight * scale;
        float scaledX = displayX + (displayWidth - scaledWidth) / 2f;
        float scaledY = displayY + (displayHeight - scaledHeight) / 2f - (1f - visibilityProgress) * 50f;
        
        // Draw shadow
        shadowPaint.setAlpha((int) (alpha * 50));
        RectF shadowRect = new RectF(
            scaledX + 8, scaledY + 10,
            scaledX + scaledWidth + 8, scaledY + scaledHeight + 10
        );
        canvas.drawRoundRect(shadowRect, 40f, 40f, shadowPaint);
        
        // Draw background with gradient
        RectF displayRect = new RectF(scaledX, scaledY, scaledX + scaledWidth, scaledY + scaledHeight);
        
        // Create gradient background
        LinearGradient bgGradient = new LinearGradient(
            scaledX, scaledY, scaledX, scaledY + scaledHeight,
            new int[] {0xFFFFFFF0, 0xFFFFF8DC},
            null,
            Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(bgGradient);
        backgroundPaint.setAlpha((int) (alpha * 255));
        canvas.drawRoundRect(displayRect, 40f, 40f, backgroundPaint);
        backgroundPaint.setShader(null);
        
        // Draw border
        borderPaint.setColor(letterColor);
        borderPaint.setAlpha((int) (alpha * 200));
        canvas.drawRoundRect(displayRect, 40f, 40f, borderPaint);
        
        // Get emoji for word
        String emoji = WORD_EMOJIS.get(currentWord);
        if (emoji == null) {
            emoji = "📝";
        }
        
        // Draw large emoji
        emojiPaint.setTextSize(scaledWidth * 0.4f);
        emojiPaint.setAlpha((int) (alpha * 255));
        float emojiY = scaledY + scaledHeight * 0.45f;
        canvas.drawText(emoji, scaledX + scaledWidth / 2f, emojiY, emojiPaint);
        
        // Draw word with highlighted first letter
        float wordY = scaledY + scaledHeight * 0.78f;
        wordPaint.setTextSize(scaledWidth * 0.12f);
        wordPaint.setAlpha((int) (alpha * 255));
        
        // Draw the word
        canvas.drawText(currentWord, scaledX + scaledWidth / 2f, wordY, wordPaint);
        
        // Draw first letter highlight (underline or different color indicator)
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(letterColor);
        highlightPaint.setAlpha((int) (alpha * 180));
        highlightPaint.setStrokeWidth(4f);
        highlightPaint.setStyle(Paint.Style.STROKE);
        
        float textWidth = wordPaint.measureText(currentWord);
        float firstLetterWidth = wordPaint.measureText(String.valueOf(currentWord.charAt(0)));
        float underlineStartX = scaledX + scaledWidth / 2f - textWidth / 2f;
        float underlineEndX = underlineStartX + firstLetterWidth;
        float underlineY = wordY + 10f;
        
        canvas.drawLine(underlineStartX, underlineY, underlineEndX, underlineY, highlightPaint);
    }
    
    /**
     * Update screen size
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        calculateDimensions();
    }
    
    public boolean isVisible() {
        return isVisible && visibilityProgress > 0;
    }
    
    // Easing functions
    private float easeOutCubic(float t) {
        return (float) (1f - Math.pow(1f - t, 3));
    }
    
    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return (float) (1f + c3 * Math.pow(t - 1f, 3) + c1 * Math.pow(t - 1f, 2));
    }
}
