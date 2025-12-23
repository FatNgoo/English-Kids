package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlashcardCarousel - Displays flashcards for a selected letter
 * Supports horizontal swipe with snap-to-center behavior
 */
public class FlashcardCarousel {
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Cards
    private List<Flashcard> cards;
    private int currentIndex = 0;
    
    // Position & Animation
    private float scrollX = 0f;
    private float targetScrollX = 0f;
    private float velocityX = 0f;
    
    // Card dimensions
    private float cardWidth;
    private float cardHeight;
    private float cardSpacing;
    private float cardY;
    
    // Touch tracking
    private float touchStartX = 0f;
    private float lastTouchX = 0f;
    private boolean isDragging = false;
    
    // Animation state
    private boolean isVisible = false;
    private float visibilityProgress = 0f;
    private float targetVisibility = 0f;
    
    // Current letter
    private char currentLetter = 'A';
    
    // Paints
    private Paint cardPaint;
    private Paint shadowPaint;
    private Paint textPaint;
    private Paint wordPaint;
    
    // Callback
    private OnCardClickListener cardClickListener;
    
    // Flashcard data for all letters
    private static final Map<Character, String[]> LETTER_WORDS = new HashMap<>();
    
    static {
        LETTER_WORDS.put('A', new String[]{"Apple", "Ant", "Airplane", "Alligator", "Astronaut", "Avocado"});
        LETTER_WORDS.put('B', new String[]{"Ball", "Bear", "Banana", "Butterfly", "Boat", "Bird"});
        LETTER_WORDS.put('C', new String[]{"Cat", "Car", "Cake", "Cow", "Carrot", "Cloud"});
        LETTER_WORDS.put('D', new String[]{"Dog", "Duck", "Donut", "Dinosaur", "Drum", "Dolphin"});
        LETTER_WORDS.put('E', new String[]{"Elephant", "Egg", "Eagle", "Earth", "Ear", "Eye"});
        LETTER_WORDS.put('F', new String[]{"Fish", "Frog", "Flower", "Fox", "Fire", "Fan"});
        LETTER_WORDS.put('G', new String[]{"Giraffe", "Grape", "Guitar", "Goat", "Gift", "Garden"});
        LETTER_WORDS.put('H', new String[]{"House", "Horse", "Heart", "Hat", "Honey", "Hamburger"});
        LETTER_WORDS.put('I', new String[]{"Ice cream", "Igloo", "Island", "Insect", "Iron", "Iris"});
        LETTER_WORDS.put('J', new String[]{"Jellyfish", "Juice", "Jacket", "Jam", "Jet", "Jungle"});
        LETTER_WORDS.put('K', new String[]{"Kite", "Kangaroo", "Key", "King", "Koala", "Kitchen"});
        LETTER_WORDS.put('L', new String[]{"Lion", "Lemon", "Lamp", "Leaf", "Ladybug", "Lizard"});
        LETTER_WORDS.put('M', new String[]{"Monkey", "Moon", "Mango", "Mouse", "Music", "Mountain"});
        LETTER_WORDS.put('N', new String[]{"Nest", "Nut", "Nose", "Noodle", "Nurse", "Notebook"});
        LETTER_WORDS.put('O', new String[]{"Orange", "Owl", "Ocean", "Octopus", "Onion", "Oven"});
        LETTER_WORDS.put('P', new String[]{"Pig", "Pizza", "Penguin", "Panda", "Piano", "Pumpkin"});
        LETTER_WORDS.put('Q', new String[]{"Queen", "Quilt", "Question", "Quail", "Quarter", "Quiet"});
        LETTER_WORDS.put('R', new String[]{"Rabbit", "Rainbow", "Robot", "Rocket", "Rose", "Rain"});
        LETTER_WORDS.put('S', new String[]{"Sun", "Star", "Snake", "Strawberry", "Ship", "Smile"});
        LETTER_WORDS.put('T', new String[]{"Tiger", "Tree", "Train", "Turtle", "Tomato", "Trumpet"});
        LETTER_WORDS.put('U', new String[]{"Umbrella", "Unicorn", "Up", "Under", "Uniform", "UFO"});
        LETTER_WORDS.put('V', new String[]{"Violin", "Vegetable", "Van", "Volcano", "Vase", "Valentine"});
        LETTER_WORDS.put('W', new String[]{"Water", "Whale", "Watermelon", "Wolf", "Window", "Watch"});
        LETTER_WORDS.put('X', new String[]{"Xylophone", "X-ray", "Fox", "Box", "Six", "Mix"});
        LETTER_WORDS.put('Y', new String[]{"Yogurt", "Yacht", "Yellow", "Yak", "Yarn", "Yo-yo"});
        LETTER_WORDS.put('Z', new String[]{"Zebra", "Zoo", "Zero", "Zipper", "Zucchini", "Zigzag"});
    }
    
    // Card colors (darker semi-transparent for dark theme)
    private static final int[] CARD_COLORS = {
        0xE6303F9F,  // Indigo
        0xE61565C0,  // Purple
        0xE60288D1,  // Light Blue
        0xE600897B,  // Teal
        0xE6558B2F,  // Light Green
        0xE6E65100   // Orange
    };
    
    public interface OnCardClickListener {
        void onCardClick(Flashcard card);
    }
    
    public FlashcardCarousel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.cards = new ArrayList<>();
        
        calculateDimensions();
        initPaints();
    }
    
    private void calculateDimensions() {
        cardWidth = screenWidth * 0.55f;
        cardHeight = cardWidth * 1.2f;
        cardSpacing = 20f;
        cardY = screenHeight - cardHeight - 80f;
    }
    
    private void initPaints() {
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setStyle(Paint.Style.FILL);
        
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(40, 0, 0, 0));
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        wordPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        wordPaint.setColor(0xFFFFFFFF);  // White for dark theme
    }
    
    /**
     * Set cards for a specific letter
     */
    public void setLetter(char letter, int letterColor) {
        this.currentLetter = letter;
        cards.clear();
        
        String[] words = LETTER_WORDS.get(letter);
        if (words == null) {
            words = new String[]{"Example"};
        }
        
        for (int i = 0; i < words.length; i++) {
            int cardColor = CARD_COLORS[i % CARD_COLORS.length];
            Flashcard card = new Flashcard(words[i], letter, cardColor, letterColor);
            cards.add(card);
        }
        
        currentIndex = 0;
        scrollX = 0;
        targetScrollX = 0;
    }
    
    /**
     * Show carousel with animation
     */
    public void show() {
        targetVisibility = 1f;
        isVisible = true;
    }
    
    /**
     * Hide carousel with animation
     */
    public void hide() {
        targetVisibility = 0f;
        isVisible = false;
        visibilityProgress = 0f;
        isDragging = false;
    }
    
    /**
     * Update carousel state
     */
    public void update(float deltaTime) {
        // Update visibility animation
        if (visibilityProgress < targetVisibility) {
            visibilityProgress = Math.min(targetVisibility, visibilityProgress + deltaTime * 3f);
        } else if (visibilityProgress > targetVisibility) {
            visibilityProgress = Math.max(targetVisibility, visibilityProgress - deltaTime * 3f);
            if (visibilityProgress <= 0) {
                isVisible = false;
            }
        }
        
        if (!isVisible) return;
        
        // Animate scroll towards target
        if (!isDragging) {
            float diff = targetScrollX - scrollX;
            scrollX += diff * deltaTime * 10f;
            
            // Snap when close enough
            if (Math.abs(diff) < 1f) {
                scrollX = targetScrollX;
            }
        }
        
        // Apply velocity when released
        if (!isDragging && Math.abs(velocityX) > 1f) {
            scrollX += velocityX * deltaTime;
            velocityX *= 0.9f;
            
            // Clamp scroll bounds
            float maxScroll = (cards.size() - 1) * (cardWidth + cardSpacing);
            scrollX = Math.max(0, Math.min(maxScroll, scrollX));
            
            // Snap to nearest card
            if (Math.abs(velocityX) < 50f) {
                snapToNearestCard();
                velocityX = 0;
            }
        }
    }
    
    /**
     * Draw carousel
     */
    public void draw(Canvas canvas) {
        if (!isVisible || visibilityProgress <= 0) return;
        
        float slideY = (1f - easeOutCubic(visibilityProgress)) * cardHeight;
        
        // Draw each card
        float centerX = screenWidth / 2f;
        float totalWidth = cards.size() * (cardWidth + cardSpacing) - cardSpacing;
        float startX = centerX - cardWidth / 2f - scrollX;
        
        for (int i = 0; i < cards.size(); i++) {
            Flashcard card = cards.get(i);
            float cardX = startX + i * (cardWidth + cardSpacing);
            
            // Calculate distance from center for scaling
            float cardCenterX = cardX + cardWidth / 2f;
            float distFromCenter = Math.abs(cardCenterX - centerX);
            float maxDist = cardWidth + cardSpacing;
            float scale = 1f - Math.min(1f, distFromCenter / maxDist) * 0.15f;
            
            // Calculate alpha based on visibility
            float alpha = visibilityProgress;
            
            drawCard(canvas, card, cardX, cardY + slideY, scale, alpha, i == currentIndex);
        }
    }
    
    private void drawCard(Canvas canvas, Flashcard card, float x, float y, float scale, float alpha, boolean isFocused) {
        float w = cardWidth * scale;
        float h = cardHeight * scale;
        float cx = x + (cardWidth - w) / 2f;
        float cy = y + (cardHeight - h) / 2f;
        
        // Draw shadow
        shadowPaint.setAlpha((int) (alpha * 40));
        RectF shadowRect = new RectF(cx + 6, cy + 8, cx + w + 6, cy + h + 8);
        canvas.drawRoundRect(shadowRect, 30f, 30f, shadowPaint);
        
        // Draw card background
        cardPaint.setColor(card.cardColor);
        cardPaint.setAlpha((int) (alpha * 255));
        RectF cardRect = new RectF(cx, cy, cx + w, cy + h);
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint);
        
        // Draw emoji/icon placeholder (large first letter)
        textPaint.setTextSize(w * 0.5f);
        textPaint.setColor(Color.argb((int) (alpha * 255), 
            Color.red(card.letterColor), 
            Color.green(card.letterColor), 
            Color.blue(card.letterColor)));
        canvas.drawText(String.valueOf(card.word.charAt(0)), 
            cx + w / 2f, cy + h * 0.45f, textPaint);
        
        // Draw word
        wordPaint.setTextSize(w * 0.14f);
        wordPaint.setAlpha((int) (alpha * 255));
        canvas.drawText(card.word, cx + w / 2f, cy + h * 0.85f, wordPaint);
        
        // Draw focus indicator
        if (isFocused && alpha > 0.9f) {
            Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            focusPaint.setStyle(Paint.Style.STROKE);
            focusPaint.setStrokeWidth(4f);
            focusPaint.setColor(Color.argb((int) (alpha * 150), 
                Color.red(card.letterColor), 
                Color.green(card.letterColor), 
                Color.blue(card.letterColor)));
            canvas.drawRoundRect(cardRect, 30f, 30f, focusPaint);
        }
    }
    
    /**
     * Handle touch down
     */
    public boolean onTouchDown(float x, float y) {
        if (!isVisible || visibilityProgress < 0.9f) return false;
        
        // Check if touch is in carousel area
        if (y >= cardY - 50) {
            touchStartX = x;
            lastTouchX = x;
            isDragging = true;
            velocityX = 0;
            return true;
        }
        return false;
    }
    
    /**
     * Handle touch move
     */
    public void onTouchMove(float x, float y) {
        if (!isDragging) return;
        
        float dx = lastTouchX - x;
        scrollX += dx;
        
        // Clamp scroll
        float maxScroll = (cards.size() - 1) * (cardWidth + cardSpacing);
        scrollX = Math.max(-cardWidth * 0.3f, Math.min(maxScroll + cardWidth * 0.3f, scrollX));
        
        velocityX = dx * 60f; // Scale for velocity
        lastTouchX = x;
    }
    
    /**
     * Handle touch up
     */
    public void onTouchUp(float x, float y) {
        if (!isDragging) return;
        isDragging = false;
        
        float dx = Math.abs(x - touchStartX);
        
        // If it was a tap (not a swipe), trigger card click
        if (dx < 20f) {
            handleCardTap(x, y);
        } else {
            // Snap to nearest card
            snapToNearestCard();
        }
    }
    
    private void snapToNearestCard() {
        float cardStep = cardWidth + cardSpacing;
        currentIndex = Math.round(scrollX / cardStep);
        currentIndex = Math.max(0, Math.min(cards.size() - 1, currentIndex));
        targetScrollX = currentIndex * cardStep;
    }
    
    private void handleCardTap(float x, float y) {
        // Find tapped card
        float centerX = screenWidth / 2f;
        float startX = centerX - cardWidth / 2f - scrollX;
        
        for (int i = 0; i < cards.size(); i++) {
            float cardX = startX + i * (cardWidth + cardSpacing);
            
            if (x >= cardX && x <= cardX + cardWidth && y >= cardY && y <= cardY + cardHeight) {
                if (cardClickListener != null) {
                    cardClickListener.onCardClick(cards.get(i));
                }
                break;
            }
        }
    }
    
    public void setOnCardClickListener(OnCardClickListener listener) {
        this.cardClickListener = listener;
    }
    
    public boolean isVisible() {
        return isVisible && visibilityProgress > 0;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public Flashcard getCurrentCard() {
        if (currentIndex >= 0 && currentIndex < cards.size()) {
            return cards.get(currentIndex);
        }
        return null;
    }
    
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        calculateDimensions();
    }
    
    private float easeOutCubic(float t) {
        return (float) (1f - Math.pow(1f - t, 3));
    }
    
    /**
     * Flashcard data class
     */
    public static class Flashcard {
        public String word;
        public char letter;
        public int cardColor;
        public int letterColor;
        
        public Flashcard(String word, char letter, int cardColor, int letterColor) {
            this.word = word;
            this.letter = letter;
            this.cardColor = cardColor;
            this.letterColor = letterColor;
        }
    }
}
