package com.edu.english.numberdash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Main game view for Number Dash Race
 * Handles game loop, rendering, and input
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    
    // Game loop
    private Thread gameThread;
    private volatile boolean isRunning = false;
    private long lastFrameTime;
    
    // Game components
    private GameState gameState;
    private ParallaxBackground background;
    private QuestionManager questionManager;
    private ConfettiSystem confettiSystem;
    
    // Characters
    private Character[] availableCharacters;
    private Character playerCharacter;
    
    // UI State
    private AnswerButton[] answerButtons;
    private int selectedCharacterIndex = 0;
    private float countdownScale = 1f;
    private String countdownText = "";
    private float feedbackAlpha = 0f;
    private String feedbackText = "";
    private int feedbackColor = 0;
    
    // Animation timers
    private float countdownAnimTimer = 0f;
    private float questionAnimTimer = 0f;
    private float resultAnimTimer = 0f;
    
    // Touch state
    private int touchedButtonIndex = -1;
    
    // Paints
    private Paint textPaint;
    private Paint panelPaint;
    private Paint buttonPaint;
    private Paint overlayPaint;
    private Paint emojiPaint;
    private Paint progressPaint;
    
    // Sound
    private SoundPool soundPool;
    private int soundCorrect;
    private int soundWrong;
    private int soundCountdown;
    private int soundFinish;
    private boolean soundsLoaded = false;
    
    // Callback
    private GameCallback callback;
    
    public interface GameCallback {
        void onGameFinished(int correctAnswers, int totalQuestions, int stars);
        void onBackPressed();
    }
    
    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        
        initGame();
        initPaints();
        initSounds(context);
    }
    
    private void initGame() {
        gameState = new GameState();
        questionManager = new QuestionManager();
        
        // Initialize available characters
        Character.CharacterType[] types = Character.CharacterType.values();
        availableCharacters = new Character[types.length];
        for (int i = 0; i < types.length; i++) {
            availableCharacters[i] = new Character(types[i]);
        }
        
        // Default selected character
        playerCharacter = availableCharacters[0];
        
        // Initialize answer buttons (will be positioned later)
        answerButtons = new AnswerButton[3];
        int[] buttonColors = {
            GameConstants.COLOR_BUTTON_1,
            GameConstants.COLOR_BUTTON_2,
            GameConstants.COLOR_BUTTON_3
        };
        for (int i = 0; i < 3; i++) {
            answerButtons[i] = new AnswerButton(buttonColors[i]);
        }
    }
    
    private void initPaints() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        panelPaint = new Paint();
        panelPaint.setAntiAlias(true);
        panelPaint.setColor(0xFFFFFFFF);
        panelPaint.setShadowLayer(10, 0, 5, 0x40000000);
        
        buttonPaint = new Paint();
        buttonPaint.setAntiAlias(true);
        
        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(true);
        
        emojiPaint = new Paint();
        emojiPaint.setAntiAlias(true);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
    }
    
    private void initSounds(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();
        
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                soundsLoaded = true;
            }
        });
        
        // For now, we'll skip actual sound loading and just set flags
        // In production, you'd load actual sound resources
        soundsLoaded = true;
    }
    
    public void setCallback(GameCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();
        
        gameState.setScreenWidth(width);
        gameState.setScreenHeight(height);
        
        // Initialize background
        background = new ParallaxBackground(width, height);
        
        // Initialize confetti
        confettiSystem = new ConfettiSystem(width, height, 100);
        
        // Position character
        float characterX = width * GameConstants.CHARACTER_START_X;
        float characterY = height * GameConstants.CHARACTER_Y - GameConstants.CHARACTER_HEIGHT / 2;
        playerCharacter.setX(characterX);
        playerCharacter.setY(characterY);
        
        // Position answer buttons
        positionAnswerButtons(width, height);
        
        // Start game thread
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        gameState.setScreenWidth(width);
        gameState.setScreenHeight(height);
        
        if (background != null) {
            background.setScreenDimensions(width, height);
        }
        if (confettiSystem != null) {
            confettiSystem.setScreenDimensions(width, height);
        }
        
        positionAnswerButtons(width, height);
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void positionAnswerButtons(int screenWidth, int screenHeight) {
        float buttonSize = GameConstants.ANSWER_BUTTON_SIZE;
        float buttonMargin = GameConstants.ANSWER_BUTTON_MARGIN;
        float totalWidth = buttonSize * 3 + buttonMargin * 2;
        float startX = (screenWidth - totalWidth) / 2;
        float buttonY = screenHeight - buttonSize - 40;
        
        for (int i = 0; i < 3; i++) {
            answerButtons[i].setPosition(
                startX + i * (buttonSize + buttonMargin),
                buttonY,
                buttonSize
            );
        }
    }
    
    @Override
    public void run() {
        lastFrameTime = System.nanoTime();
        
        while (isRunning) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1000000f; // Convert to ms
            lastFrameTime = currentTime;
            
            // Cap delta time to prevent physics issues
            if (deltaTime > 50) deltaTime = 50;
            
            update(deltaTime);
            render();
            
            // Frame rate control
            long frameTime = System.nanoTime() - currentTime;
            long sleepTime = (GameConstants.FRAME_TIME_NS - frameTime) / 1000000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void update(float deltaTime) {
        int state = gameState.getCurrentState();
        
        switch (state) {
            case GameConstants.STATE_CHARACTER_SELECT:
                updateCharacterSelect(deltaTime);
                break;
            case GameConstants.STATE_COUNTDOWN:
                updateCountdown(deltaTime);
                break;
            case GameConstants.STATE_RACING:
            case GameConstants.STATE_QUESTION:
                updateRacing(deltaTime);
                break;
            case GameConstants.STATE_RESULT:
                updateResult(deltaTime);
                break;
        }
        
        // Update feedback animation
        if (feedbackAlpha > 0) {
            feedbackAlpha -= deltaTime * 0.002f;
            if (feedbackAlpha < 0) feedbackAlpha = 0;
        }
        
        // Update answer buttons
        for (AnswerButton button : answerButtons) {
            button.update(deltaTime);
        }
    }
    
    private void updateCharacterSelect(float deltaTime) {
        // Update idle animation for selected character
        for (int i = 0; i < availableCharacters.length; i++) {
            availableCharacters[i].update(deltaTime, gameState);
        }
    }
    
    private void updateCountdown(float deltaTime) {
        countdownAnimTimer += deltaTime;
        
        float secondProgress = countdownAnimTimer / 1000f;
        
        if (secondProgress >= 1f) {
            countdownAnimTimer = 0;
            int count = gameState.getCountdownValue();
            count--;
            gameState.setCountdownValue(count);
            
            if (count <= 0) {
                // Start race
                gameState.setCurrentState(GameConstants.STATE_RACING);
                countdownText = "GO!";
                countdownScale = GameConstants.COUNTDOWN_SCALE_START;
            } else {
                countdownText = String.valueOf(count);
                countdownScale = GameConstants.COUNTDOWN_SCALE_START;
                playSound(soundCountdown);
            }
        } else {
            // Animate scale
            float t = EasingUtils.easeOutElastic(secondProgress);
            countdownScale = EasingUtils.lerp(GameConstants.COUNTDOWN_SCALE_START, 
                                              GameConstants.COUNTDOWN_SCALE_END, t);
        }
        
        // Update character
        playerCharacter.update(deltaTime, gameState);
        background.update(deltaTime, 0); // No movement during countdown
    }
    
    private void updateRacing(float deltaTime) {
        // Update game state
        gameState.update(deltaTime);
        
        // Update background parallax
        background.update(deltaTime, gameState.getCurrentSpeed());
        
        // Update character
        playerCharacter.update(deltaTime, gameState);
        
        // Add slight horizontal bobbing when running (creates illusion of forward movement)
        if (!gameState.isShowingQuestion()) {
            float bobPhase = (float) Math.sin(System.currentTimeMillis() * 0.01);
            float bobOffset = bobPhase * 3f;
            playerCharacter.setX(gameState.getScreenWidth() * GameConstants.CHARACTER_START_X + bobOffset);
        }
        
        // Check for new question
        if (!gameState.isShowingQuestion() && gameState.shouldShowQuestion()) {
            showNextQuestion();
        }
        
        // Update question animation
        if (gameState.isShowingQuestion()) {
            questionAnimTimer += deltaTime;
        }
        
        // Check for race finish
        if (gameState.isRaceFinished()) {
            gameState.setCurrentState(GameConstants.STATE_RESULT);
            resultAnimTimer = 0;
            confettiSystem.start();
            playSound(soundFinish);
        }
    }
    
    private void updateResult(float deltaTime) {
        resultAnimTimer += deltaTime;
        confettiSystem.update(deltaTime);
        playerCharacter.update(deltaTime, gameState);
    }
    
    private void showNextQuestion() {
        Question question = questionManager.getNextQuestion();
        if (question != null) {
            gameState.showQuestion();
            questionAnimTimer = 0;
            
            // Update button values
            int[] options = question.getOptions();
            for (int i = 0; i < answerButtons.length && i < options.length; i++) {
                answerButtons[i].setValue(options[i]);
            }
        }
    }
    
    private void render() {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas != null) {
                synchronized (getHolder()) {
                    draw(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        int state = gameState.getCurrentState();
        
        switch (state) {
            case GameConstants.STATE_CHARACTER_SELECT:
                drawCharacterSelect(canvas);
                break;
            case GameConstants.STATE_COUNTDOWN:
                drawCountdown(canvas);
                break;
            case GameConstants.STATE_RACING:
            case GameConstants.STATE_QUESTION:
                drawRacing(canvas);
                break;
            case GameConstants.STATE_RESULT:
                drawResult(canvas);
                break;
        }
    }
    
    private void drawCharacterSelect(Canvas canvas) {
        int width = gameState.getScreenWidth();
        int height = gameState.getScreenHeight();
        
        // Draw background
        if (background != null) {
            background.draw(canvas);
        }
        
        // Semi-transparent overlay
        overlayPaint.setColor(0x80000000);
        canvas.drawRect(0, 0, width, height, overlayPaint);
        
        // Title panel
        float panelWidth = width * 0.8f;
        float panelHeight = height * 0.75f;
        float panelX = (width - panelWidth) / 2;
        float panelY = (height - panelHeight) / 2;
        
        panelPaint.setColor(0xFFFFFFFF);
        RectF panelRect = new RectF(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        canvas.drawRoundRect(panelRect, 30, 30, panelPaint);
        
        // Title
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Choose Your Racer!", width / 2f, panelY + 60, textPaint);
        
        // Draw character options
        float charAreaY = panelY + 100;
        float charAreaHeight = panelHeight - 200;
        float charSize = Math.min(charAreaHeight * 0.6f, (panelWidth - 60) / availableCharacters.length);
        float startX = panelX + (panelWidth - (charSize * availableCharacters.length + 20 * (availableCharacters.length - 1))) / 2;
        
        for (int i = 0; i < availableCharacters.length; i++) {
            float charX = startX + i * (charSize + 20);
            float charY = charAreaY + (charAreaHeight - charSize) / 2;
            
            // Selection highlight
            if (i == selectedCharacterIndex) {
                buttonPaint.setColor(0xFF4CAF50);
                RectF selectRect = new RectF(charX - 10, charY - 10, 
                                            charX + charSize + 10, charY + charSize + 30);
                canvas.drawRoundRect(selectRect, 20, 20, buttonPaint);
            }
            
            // Character background
            buttonPaint.setColor(0xFFF5F5F5);
            RectF charRect = new RectF(charX, charY, charX + charSize, charY + charSize);
            canvas.drawRoundRect(charRect, 15, 15, buttonPaint);
            
            // Draw character
            canvas.save();
            float scale = charSize / GameConstants.CHARACTER_WIDTH;
            canvas.translate(charX, charY);
            canvas.scale(scale, scale);
            
            Character c = availableCharacters[i];
            c.setX(0);
            c.setY(0);
            c.draw(canvas, buttonPaint);
            
            canvas.restore();
            
            // Character name
            textPaint.setTextSize(20);
            textPaint.setColor(i == selectedCharacterIndex ? 0xFFFFFFFF : 0xFF666666);
            canvas.drawText(c.getName(), charX + charSize / 2, charY + charSize + 20, textPaint);
        }
        
        // Start button
        float buttonWidth = 200;
        float buttonHeight = 60;
        float buttonX = (width - buttonWidth) / 2;
        float buttonY = panelY + panelHeight - 80;
        
        buttonPaint.setColor(0xFF4CAF50);
        RectF startButton = new RectF(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight);
        canvas.drawRoundRect(startButton, 30, 30, buttonPaint);
        
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(28);
        canvas.drawText("START RACE!", width / 2f, buttonY + 40, textPaint);
    }
    
    private void drawCountdown(Canvas canvas) {
        // Draw game background
        if (background != null) {
            background.draw(canvas);
        }
        
        // Draw character
        playerCharacter.draw(canvas, buttonPaint);
        
        // Draw countdown
        int width = gameState.getScreenWidth();
        int height = gameState.getScreenHeight();
        
        // Countdown number
        canvas.save();
        canvas.translate(width / 2f, height / 2f);
        canvas.scale(countdownScale, countdownScale);
        
        // Shadow
        textPaint.setColor(0x40000000);
        textPaint.setTextSize(150);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(countdownText, 5, 5, textPaint);
        
        // Main text
        if (countdownText.equals("GO!")) {
            textPaint.setColor(0xFF4CAF50);
        } else {
            textPaint.setColor(0xFFFFFFFF);
        }
        canvas.drawText(countdownText, 0, 0, textPaint);
        
        canvas.restore();
    }
    
    private void drawRacing(Canvas canvas) {
        int width = gameState.getScreenWidth();
        int height = gameState.getScreenHeight();
        
        // Draw background
        if (background != null) {
            background.draw(canvas);
            
            // Draw finish line when race is ending
            if (gameState.getQuestionsAnswered() >= GameConstants.TOTAL_QUESTIONS - 1) {
                float finishProgress = (gameState.getQuestionsAnswered() - GameConstants.TOTAL_QUESTIONS + 2) * 0.5f;
                float finishX = width + 100 - finishProgress * (width + 200);
                background.drawFinishLine(canvas, finishX);
            }
        }
        
        // Draw character
        playerCharacter.draw(canvas, buttonPaint);
        
        // Draw progress bar
        drawProgressBar(canvas, width, height);
        
        // Draw feedback text
        if (feedbackAlpha > 0) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(60);
            int alpha = (int)(feedbackAlpha * 255);
            textPaint.setColor((alpha << 24) | (feedbackColor & 0x00FFFFFF));
            
            float feedbackY = height * 0.4f - (1 - feedbackAlpha) * 50;
            canvas.drawText(feedbackText, width / 2f, feedbackY, textPaint);
        }
        
        // Draw question if showing
        if (gameState.isShowingQuestion()) {
            drawQuestion(canvas, width, height);
            drawAnswerButtons(canvas);
        }
    }
    
    private void drawProgressBar(Canvas canvas, int width, int height) {
        float barWidth = width * 0.4f;
        float barHeight = 20;
        float barX = 20;
        float barY = 20;
        
        // Background
        progressPaint.setColor(0x40000000);
        RectF bgRect = new RectF(barX, barY, barX + barWidth, barY + barHeight);
        canvas.drawRoundRect(bgRect, barHeight / 2, barHeight / 2, progressPaint);
        
        // Progress
        float progress = gameState.getRaceProgress();
        progressPaint.setColor(0xFF4CAF50);
        RectF progressRect = new RectF(barX, barY, barX + barWidth * progress, barY + barHeight);
        canvas.drawRoundRect(progressRect, barHeight / 2, barHeight / 2, progressPaint);
        
        // Question count
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.LEFT);
        String countText = gameState.getQuestionsAnswered() + "/" + GameConstants.TOTAL_QUESTIONS;
        canvas.drawText(countText, barX + barWidth + 15, barY + barHeight - 2, textPaint);
        
        // Correct answers
        textPaint.setColor(0xFF4CAF50);
        String correctText = "✓ " + gameState.getCorrectAnswers();
        canvas.drawText(correctText, barX, barY + barHeight + 25, textPaint);
    }
    
    private void drawQuestion(Canvas canvas, int width, int height) {
        Question question = questionManager.getCurrentQuestion();
        if (question == null) return;
        
        // Question panel
        float panelWidth = width * 0.7f;
        float panelHeight = 180;
        float panelX = (width - panelWidth) / 2;
        float panelY = 30;
        
        // Animate panel entrance
        float animProgress = Math.min(1f, questionAnimTimer / 300f);
        float easedProgress = EasingUtils.easeOutOvershoot(animProgress);
        float scale = easedProgress;
        
        canvas.save();
        canvas.translate(panelX + panelWidth / 2, panelY + panelHeight / 2);
        canvas.scale(scale, scale);
        canvas.translate(-panelWidth / 2, -panelHeight / 2);
        
        // Panel background
        panelPaint.setColor(0xFFFFFFFF);
        RectF panelRect = new RectF(0, 0, panelWidth, panelHeight);
        canvas.drawRoundRect(panelRect, 25, 25, panelPaint);
        
        // Visual representation (emojis)
        emojiPaint.setTextSize(50);
        emojiPaint.setColor(0xFF333333);
        canvas.drawText(question.getVisualRepresentation(), panelWidth / 2, 70, emojiPaint);
        
        // Question text
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(question.getQuestionText(), panelWidth / 2, 130, textPaint);
        
        canvas.restore();
    }
    
    private void drawAnswerButtons(Canvas canvas) {
        for (AnswerButton button : answerButtons) {
            button.draw(canvas, buttonPaint, textPaint);
        }
    }
    
    private void drawResult(Canvas canvas) {
        int width = gameState.getScreenWidth();
        int height = gameState.getScreenHeight();
        
        // Draw background
        if (background != null) {
            background.draw(canvas);
        }
        
        // Draw confetti
        if (confettiSystem != null) {
            confettiSystem.draw(canvas, buttonPaint);
        }
        
        // Result panel
        float animProgress = Math.min(1f, resultAnimTimer / 500f);
        float easedProgress = EasingUtils.easeOutOvershoot(animProgress);
        
        float panelWidth = width * 0.6f;
        float panelHeight = height * 0.65f;
        float panelX = (width - panelWidth) / 2;
        float panelY = (height - panelHeight) / 2;
        
        canvas.save();
        canvas.translate(panelX + panelWidth / 2, panelY + panelHeight / 2);
        canvas.scale(easedProgress, easedProgress);
        canvas.translate(-panelWidth / 2, -panelHeight / 2);
        
        // Panel background
        panelPaint.setColor(0xFFFFFFFF);
        RectF panelRect = new RectF(0, 0, panelWidth, panelHeight);
        canvas.drawRoundRect(panelRect, 30, 30, panelPaint);
        
        // Trophy/celebration
        emojiPaint.setTextSize(80);
        canvas.drawText("🏆", panelWidth / 2, 80, emojiPaint);
        
        // You win text
        textPaint.setColor(0xFF4CAF50);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("YOU WIN!", panelWidth / 2, 140, textPaint);
        
        // Stars
        int stars = gameState.getStarRating();
        float starY = 200;
        String starText = "";
        for (int i = 0; i < 3; i++) {
            if (i < stars) {
                starText += "⭐";
            } else {
                starText += "☆";
            }
        }
        emojiPaint.setTextSize(60);
        canvas.drawText(starText, panelWidth / 2, starY, emojiPaint);
        
        // Score
        textPaint.setColor(0xFF666666);
        textPaint.setTextSize(28);
        String scoreText = gameState.getCorrectAnswers() + " / " + GameConstants.TOTAL_QUESTIONS + " correct!";
        canvas.drawText(scoreText, panelWidth / 2, 270, textPaint);
        
        // Celebration message
        String[] messages = {"Amazing!", "Great Job!", "Fantastic!", "You're a Star!", "Wonderful!"};
        String message = messages[Math.min(stars - 1, messages.length - 1) + (int)(Math.random() * 2)];
        textPaint.setColor(0xFFFF9800);
        textPaint.setTextSize(32);
        canvas.drawText(message, panelWidth / 2, 320, textPaint);
        
        canvas.restore();
        
        // Draw buttons outside the scaled panel
        float buttonY = panelY + panelHeight - 60;
        float buttonWidth = 150;
        float buttonHeight = 50;
        float buttonSpacing = 30;
        
        // Play Again button
        float playAgainX = panelX + panelWidth / 2 - buttonWidth - buttonSpacing / 2;
        buttonPaint.setColor(0xFF4CAF50);
        RectF playAgainRect = new RectF(playAgainX, buttonY, playAgainX + buttonWidth, buttonY + buttonHeight);
        canvas.drawRoundRect(playAgainRect, 25, 25, buttonPaint);
        
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(22);
        canvas.drawText("Play Again", playAgainX + buttonWidth / 2, buttonY + 33, textPaint);
        
        // Home button
        float homeX = panelX + panelWidth / 2 + buttonSpacing / 2;
        buttonPaint.setColor(0xFF2196F3);
        RectF homeRect = new RectF(homeX, buttonY, homeX + buttonWidth, buttonY + buttonHeight);
        canvas.drawRoundRect(homeRect, 25, 25, buttonPaint);
        
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText("Home", homeX + buttonWidth / 2, buttonY + 33, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(x, y);
                break;
            case MotionEvent.ACTION_UP:
                handleTouchUp(x, y);
                break;
            case MotionEvent.ACTION_CANCEL:
                touchedButtonIndex = -1;
                for (AnswerButton button : answerButtons) {
                    button.setPressed(false);
                }
                break;
        }
        
        return true;
    }
    
    private void handleTouchDown(float x, float y) {
        int state = gameState.getCurrentState();
        
        if (state == GameConstants.STATE_RACING && gameState.isShowingQuestion()) {
            // Check answer buttons during racing with question
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].contains(x, y)) {
                    touchedButtonIndex = i;
                    answerButtons[i].setPressed(true);
                    break;
                }
            }
        } else if (state == GameConstants.STATE_CHARACTER_SELECT) {
            // Check character selection
            float panelWidth = gameState.getScreenWidth() * 0.8f;
            float panelHeight = gameState.getScreenHeight() * 0.75f;
            float panelX = (gameState.getScreenWidth() - panelWidth) / 2;
            float panelY = (gameState.getScreenHeight() - panelHeight) / 2;
            
            float charAreaY = panelY + 100;
            float charAreaHeight = panelHeight - 200;
            float charSize = Math.min(charAreaHeight * 0.6f, 
                (panelWidth - 60) / availableCharacters.length);
            float startX = panelX + (panelWidth - (charSize * availableCharacters.length + 
                20 * (availableCharacters.length - 1))) / 2;
            
            for (int i = 0; i < availableCharacters.length; i++) {
                float charX = startX + i * (charSize + 20);
                float charY = charAreaY + (charAreaHeight - charSize) / 2;
                
                if (x >= charX && x <= charX + charSize && 
                    y >= charY && y <= charY + charSize + 30) {
                    selectedCharacterIndex = i;
                    playerCharacter = availableCharacters[i];
                    break;
                }
            }
        }
    }
    
    private void handleTouchUp(float x, float y) {
        int state = gameState.getCurrentState();
        
        if (state == GameConstants.STATE_CHARACTER_SELECT) {
            // Check start button
            float buttonWidth = 200;
            float buttonHeight = 60;
            float buttonX = (gameState.getScreenWidth() - buttonWidth) / 2;
            float panelHeight = gameState.getScreenHeight() * 0.75f;
            float panelY = (gameState.getScreenHeight() - panelHeight) / 2;
            float buttonY = panelY + panelHeight - 80;
            
            if (x >= buttonX && x <= buttonX + buttonWidth &&
                y >= buttonY && y <= buttonY + buttonHeight) {
                // Start game
                startRace();
            }
        } else if (state == GameConstants.STATE_RACING && gameState.isShowingQuestion()) {
            // Check answer buttons during racing
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setPressed(false);
                
                if (touchedButtonIndex == i && answerButtons[i].contains(x, y)) {
                    // Answer selected
                    int selectedValue = answerButtons[i].getValue();
                    boolean correct = questionManager.checkAnswer(selectedValue);
                    
                    gameState.answerQuestion(correct);
                    
                    if (correct) {
                        feedbackText = "Great!";
                        feedbackColor = GameConstants.COLOR_CORRECT;
                        playSound(soundCorrect);
                    } else {
                        feedbackText = "Oops!";
                        feedbackColor = GameConstants.COLOR_WRONG;
                        playSound(soundWrong);
                    }
                    feedbackAlpha = 1f;
                }
            }
            touchedButtonIndex = -1;
        } else if (state == GameConstants.STATE_RESULT) {
            // Check result buttons
            float panelWidth = gameState.getScreenWidth() * 0.6f;
            float panelHeight = gameState.getScreenHeight() * 0.65f;
            float panelX = (gameState.getScreenWidth() - panelWidth) / 2;
            float panelY = (gameState.getScreenHeight() - panelHeight) / 2;
            float buttonY = panelY + panelHeight - 60;
            float buttonWidth = 150;
            float buttonHeight = 50;
            float buttonSpacing = 30;
            
            float playAgainX = panelX + panelWidth / 2 - buttonWidth - buttonSpacing / 2;
            float homeX = panelX + panelWidth / 2 + buttonSpacing / 2;
            
            if (x >= playAgainX && x <= playAgainX + buttonWidth &&
                y >= buttonY && y <= buttonY + buttonHeight) {
                // Play again
                resetGame();
            } else if (x >= homeX && x <= homeX + buttonWidth &&
                       y >= buttonY && y <= buttonY + buttonHeight) {
                // Go home
                if (callback != null) {
                    callback.onGameFinished(gameState.getCorrectAnswers(), 
                        GameConstants.TOTAL_QUESTIONS, gameState.getStarRating());
                }
            }
        }
    }
    
    private void startRace() {
        // Position character for race
        float characterX = gameState.getScreenWidth() * GameConstants.CHARACTER_START_X;
        float characterY = gameState.getScreenHeight() * GameConstants.CHARACTER_Y - 
                          GameConstants.CHARACTER_HEIGHT / 2;
        playerCharacter.setX(characterX);
        playerCharacter.setY(characterY);
        
        // Start countdown
        gameState.setCurrentState(GameConstants.STATE_COUNTDOWN);
        gameState.setCountdownValue(GameConstants.COUNTDOWN_SECONDS);
        countdownText = String.valueOf(GameConstants.COUNTDOWN_SECONDS);
        countdownScale = GameConstants.COUNTDOWN_SCALE_START;
        countdownAnimTimer = 0;
        
        playSound(soundCountdown);
    }
    
    private void resetGame() {
        gameState.reset();
        questionManager.reset();
        confettiSystem.stop();
        gameState.setCurrentState(GameConstants.STATE_CHARACTER_SELECT);
    }
    
    private void playSound(int soundId) {
        if (soundsLoaded && soundPool != null) {
            // soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
            // Sound playing is optional - game works without sounds
        }
    }
    
    public void cleanup() {
        isRunning = false;
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
    
    /**
     * Inner class for answer buttons
     */
    private static class AnswerButton {
        private float x, y, size;
        private int value;
        private int color;
        private boolean pressed = false;
        private float scale = 1f;
        private float targetScale = 1f;
        
        public AnswerButton(int color) {
            this.color = color;
        }
        
        public void setPosition(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public void setPressed(boolean pressed) {
            this.pressed = pressed;
            targetScale = pressed ? GameConstants.BUTTON_PRESS_SCALE : 1f;
        }
        
        public boolean contains(float px, float py) {
            return px >= x && px <= x + size && py >= y && py <= y + size;
        }
        
        public void update(float deltaTime) {
            // Smooth scale animation
            if (scale != targetScale) {
                float diff = targetScale - scale;
                scale += diff * 0.2f;
                if (Math.abs(scale - targetScale) < 0.01f) {
                    scale = targetScale;
                }
            }
        }
        
        public void draw(Canvas canvas, Paint buttonPaint, Paint textPaint) {
            canvas.save();
            
            float centerX = x + size / 2;
            float centerY = y + size / 2;
            canvas.translate(centerX, centerY);
            canvas.scale(scale, scale);
            canvas.translate(-size / 2, -size / 2);
            
            // Shadow
            buttonPaint.setColor(0x40000000);
            RectF shadowRect = new RectF(5, 5, size + 5, size + 5);
            canvas.drawRoundRect(shadowRect, size / 2, size / 2, buttonPaint);
            
            // Button
            buttonPaint.setColor(color);
            RectF buttonRect = new RectF(0, 0, size, size);
            canvas.drawRoundRect(buttonRect, size / 2, size / 2, buttonPaint);
            
            // Highlight
            buttonPaint.setColor(0x20FFFFFF);
            RectF highlightRect = new RectF(5, 5, size - 5, size / 2);
            canvas.drawRoundRect(highlightRect, size / 4, size / 4, buttonPaint);
            
            // Text
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextSize(size * 0.4f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(value), size / 2, size / 2 + size * 0.15f, textPaint);
            
            canvas.restore();
        }
    }
}
