# MASTER CHEF - ENGLISH COOKING ADVENTURE
## Design Document & Architecture

---

## 1. UI FLOW DIAGRAM

```
MainActivity (Home/Games)
    ↓ [Click Master Chef]
MasterChefMapActivity (Level Selection Map)
    ↓ [Click Level Node]
LevelIntroDialog (Popup: Dish Info + Start Button)
    ↓ [Click Start]
MasterChefGameActivity (Container for Scene Fragments)
    ├─→ Scene1Fragment (Restaurant Ordering)
    │       ↓ [Order Complete]
    ├─→ Scene2Fragment (Supermarket Shopping)
    │       ↓ [Shopping Complete]
    ├─→ Scene3Fragment (Kitchen Cooking)
    │       ↓ [Cooking Complete]
    └─→ LevelCompleteDialog (Stars + Rewards)
            ↓ [Dismiss]
        Back to MasterChefMapActivity
        OR
        Open CookbookActivity (Button)
```

---

## 2. ARCHITECTURE OVERVIEW (MVVM + State Machine)

### Package Structure
```
com.edu.english.masterchef/
├── data/
│   ├── model/
│   │   ├── Food.java
│   │   ├── Ingredient.java
│   │   ├── LevelConfig.java
│   │   ├── SceneScript.java
│   │   ├── CookingStep.java
│   │   ├── PlayerProgress.java
│   │   └── DialogLine.java
│   ├── repository/
│   │   ├── LevelRepository.java
│   │   └── ProgressRepository.java
│   └── local/
│       └── MasterChefPreferences.java
├── ui/
│   ├── map/
│   │   ├── MasterChefMapActivity.java
│   │   ├── MasterChefMapViewModel.java
│   │   ├── LevelNodeView.java (Custom View)
│   │   └── LevelIntroDialog.java
│   ├── game/
│   │   ├── MasterChefGameActivity.java
│   │   ├── GameViewModel.java
│   │   ├── Scene1Fragment.java (Ordering)
│   │   ├── Scene1ViewModel.java
│   │   ├── Scene2Fragment.java (Shopping)
│   │   ├── Scene2ViewModel.java
│   │   ├── Scene3Fragment.java (Cooking)
│   │   ├── Scene3ViewModel.java
│   │   └── LevelCompleteDialog.java
│   └── cookbook/
│       ├── CookbookActivity.java
│       ├── CookbookViewModel.java
│       └── CookbookPageAdapter.java
├── engine/
│   ├── GameStateMachine.java
│   ├── SceneManager.java
│   ├── StepValidator.java
│   └── ScoreCalculator.java
└── util/
    ├── TTSManager.java
    ├── SoundPoolManager.java
    ├── AnimationHelper.java
    ├── KaraokeHelper.java
    └── GestureDetectorHelper.java
```

### State Machine Flow
```
GAME_STATES:
  IDLE → MAP_LOADED → LEVEL_SELECTED → ORDERING → SHOPPING → COOKING → COMPLETED

SCENE_STATES (for Scene3 - Cooking):
  SETUP → STEP_ACTIVE → STEP_VALIDATING → STEP_COMPLETE → ALL_STEPS_DONE
```

---

## 3. SCREENS & ACTIVITIES/FRAGMENTS

### 3.1 MasterChefMapActivity
**Purpose**: Level selection map with lock/unlock nodes

**XML**: `activity_masterchef_map.xml`
- Root: CoordinatorLayout (handles notch/cutout)
- MotionLayout (parallax backgrounds + camera pan)
- ScrollView (vertical/horizontal scrolling for level nodes)
- Level nodes: Custom LevelNodeView (LinearLayout + ImageView + TextView)

**ViewModel**: `MasterChefMapViewModel`
- LiveData<List<LevelConfig>> levels
- LiveData<PlayerProgress> progress
- Methods: loadLevels(), checkLevelUnlocked(levelId), onLevelClicked(levelId)

---

### 3.2 Scene1Fragment (Restaurant Ordering)
**Purpose**: Character enters restaurant, orders food

**XML**: `fragment_scene1_ordering.xml`
- Root: ConstraintLayout
- Background: ImageView (restaurant interior)
- Character: ImageView (animatable position via MotionLayout)
- NPC: ImageView (chef/waiter)
- Speech bubbles: CardView (TextView with TTS highlight)
- Menu: RecyclerView (dish items - for expansion, 1 item for MVP)
- Replay button: FloatingActionButton

**ViewModel**: `Scene1ViewModel`
- LiveData<DialogLine> currentDialogLine
- LiveData<Boolean> orderComplete
- Methods: startDialog(), selectDish(dishId), proceedToNextScene()

---

### 3.3 Scene2Fragment (Supermarket Shopping)
**Purpose**: Pick correct ingredients from shelves

**XML**: `fragment_scene2_shopping.xml`
- Root: ConstraintLayout
- Background: MotionLayout (camera follow character)
- Character: ImageView
- Chef request bubble: CardView (TextView with bullet points)
- Ingredient grid: RecyclerView (GridLayoutManager)
- Cart: LinearLayout (bottom, shows collected items)
- Submit button: MaterialButton

**ViewModel**: `Scene2ViewModel`
- LiveData<List<Ingredient>> requiredIngredients
- LiveData<List<Ingredient>> availableIngredients (includes distractors)
- LiveData<List<Ingredient>> selectedIngredients
- LiveData<Boolean> canSubmit
- Methods: onIngredientClicked(ingredient), validateCart(), proceedToScene3()

---

### 3.4 Scene3Fragment (Kitchen Cooking)
**Purpose**: Multi-step cooking with drag/drop, tap, hold, gestures

**XML**: `fragment_scene3_cooking.xml`
- Root: ConstraintLayout
- Background: ImageView (kitchen)
- Zones (FrameLayout with IDs):
  - zone_cutting (cutting board area)
  - zone_stove (stove area)
  - zone_oven (oven area)
  - zone_counter (prep counter)
  - zone_plate (serving plate)
- Ingredient tray: HorizontalScrollView (bottom, shows current step items)
- Tool tray: LinearLayout (knife, pan, pot, bowl, etc.)
- Step instruction: CardView (TextView + ImageView hint)
- Progress bar: LinearProgressIndicator
- Hold progress (pour/stir): CircularProgressIndicator (overlay when active)

**ViewModel**: `Scene3ViewModel`
- LiveData<CookingStep> currentStep
- LiveData<Integer> stepIndex
- LiveData<Float> holdProgress (for hold gestures)
- LiveData<Boolean> cookingComplete
- Methods: 
  - onItemDroppedToZone(item, zone)
  - onZoneTapped(zone)
  - onItemTapped(item)
  - onHoldStart(item, zone)
  - onHoldProgress(progress)
  - onHoldEnd()
  - onStirGesture(zone, circularProgress)
  - validateStep()

---

### 3.5 CookbookActivity
**Purpose**: Album of unlocked dishes

**XML**: `activity_cookbook.xml`
- Root: CoordinatorLayout
- Toolbar: MaterialToolbar (back button)
- ViewPager2 (page transform animation)
- Page indicator: TabLayout or custom dots

**Page Layout**: `item_cookbook_page.xml`
- CardView
  - Dish image: ImageView
  - Dish name: TextView (TTS on click)
  - Ingredients list: RecyclerView (each ingredient clickable for TTS)

**ViewModel**: `CookbookViewModel`
- LiveData<List<Food>> unlockedDishes
- Methods: getDishes(), playDishName(dishId), playIngredientName(ingredientId)

---

## 4. DATA MODELS (Java Classes)

### Food.java
```java
public class Food {
    private String dishId;
    private String name;
    private String imageRes; // drawable name
    private String theme; // "italian", "chinese", "mexican"
    private List<String> ingredientIds;
    // getters/setters
}
```

### Ingredient.java
```java
public class Ingredient {
    private String ingredientId;
    private String name;
    private String iconRes; // drawable name
    private String category; // "vegetable", "meat", "spice"
    // getters/setters
}
```

### LevelConfig.java
```java
public class LevelConfig {
    private int levelId;
    private String dishId;
    private String difficulty; // "easy", "normal", "hard"
    private int unlockCondition; // previous level id (0 for first)
    private int star1Threshold; // score for 1 star
    private int star2Threshold; // score for 2 stars
    private int star3Threshold; // score for 3 stars
    private SceneScript scene1Script;
    private SceneScript scene2Script;
    private SceneScript scene3Script;
    // getters/setters
}
```

### SceneScript.java
```java
public class SceneScript {
    private List<DialogLine> dialogLines; // Scene1
    private List<String> requiredIngredientIds; // Scene2
    private List<String> distractorIngredientIds; // Scene2
    private List<CookingStep> cookingSteps; // Scene3
    // getters/setters
}
```

### DialogLine.java
```java
public class DialogLine {
    private String speaker; // "player", "npc"
    private String text; // "I want to order Spaghetti Bolognese."
    private String ttsText; // same or modified for TTS
    private int delayMs; // delay before showing
    // getters/setters
}
```

### CookingStep.java
```java
public class CookingStep {
    private String stepType; // "DragDropToZone", "TapOnZone", "HoldToPour", "StirGesture", "PanShakeGesture", "TimerWait", "PlateServe"
    private String itemId; // ingredient or tool id
    private String targetZone; // "zone_cutting", "zone_stove", etc.
    private int requiredCount; // for tap count
    private int requiredHoldMs; // for hold duration
    private String instructionText; // "Drop tomatoes into pot", "Tap to crack egg"
    private String hintImageRes; // hint icon drawable
    // getters/setters
}
```

### PlayerProgress.java
```java
public class PlayerProgress {
    private List<Integer> unlockedLevels;
    private Map<Integer, Integer> levelStars; // levelId → stars (0-3)
    private List<String> unlockedDishIds; // for cookbook
    // getters/setters
}
```

---

## 5. CORE LOGIC (Pseudocode)

### GameStateMachine.java
```java
public class GameStateMachine {
    private GameState currentState = GameState.IDLE;
    private List<StateChangeListener> listeners;

    public enum GameState {
        IDLE, MAP_LOADED, LEVEL_SELECTED, ORDERING, SHOPPING, COOKING, COMPLETED
    }

    public void transitionTo(GameState newState) {
        if (isValidTransition(currentState, newState)) {
            GameState oldState = currentState;
            currentState = newState;
            notifyListeners(oldState, newState);
        }
    }

    private boolean isValidTransition(GameState from, GameState to) {
        // Define valid transitions
        // IDLE → MAP_LOADED
        // MAP_LOADED → LEVEL_SELECTED
        // LEVEL_SELECTED → ORDERING
        // ORDERING → SHOPPING
        // SHOPPING → COOKING
        // COOKING → COMPLETED
        // COMPLETED → MAP_LOADED
        return true; // implement logic
    }
}
```

### SceneManager.java
```java
public class SceneManager {
    private GameStateMachine stateMachine;
    private FragmentManager fragmentManager;

    public void loadScene(GameState sceneState, LevelConfig config) {
        Fragment fragment = null;
        switch (sceneState) {
            case ORDERING:
                fragment = Scene1Fragment.newInstance(config.getScene1Script());
                break;
            case SHOPPING:
                fragment = Scene2Fragment.newInstance(config.getScene2Script());
                break;
            case COOKING:
                fragment = Scene3Fragment.newInstance(config.getScene3Script());
                break;
        }
        if (fragment != null) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.game_container, fragment)
                .commit();
        }
    }
}
```

### StepValidator.java (Scene3 Logic)
```java
public class StepValidator {
    
    public ValidationResult validate(CookingStep step, PlayerAction action) {
        switch (step.getStepType()) {
            case "DragDropToZone":
                return validateDragDrop(step, action);
            case "TapOnZone":
                return validateTap(step, action);
            case "HoldToPour":
                return validateHold(step, action);
            case "StirGesture":
                return validateStir(step, action);
            // ... other cases
        }
        return ValidationResult.INVALID;
    }

    private ValidationResult validateDragDrop(CookingStep step, PlayerAction action) {
        if (action.getType() == ActionType.DROP &&
            action.getItemId().equals(step.getItemId()) &&
            action.getTargetZone().equals(step.getTargetZone())) {
            return ValidationResult.SUCCESS;
        }
        return ValidationResult.INCORRECT;
    }

    private ValidationResult validateHold(CookingStep step, PlayerAction action) {
        if (action.getType() == ActionType.HOLD_COMPLETE &&
            action.getItemId().equals(step.getItemId()) &&
            action.getHoldDuration() >= step.getRequiredHoldMs()) {
            return ValidationResult.SUCCESS;
        } else if (action.getType() == ActionType.HOLD_CANCEL) {
            return ValidationResult.CANCELLED;
        }
        return ValidationResult.IN_PROGRESS;
    }
}

public class ValidationResult {
    public static final ValidationResult SUCCESS = new ValidationResult(Status.SUCCESS);
    public static final ValidationResult INCORRECT = new ValidationResult(Status.INCORRECT);
    public static final ValidationResult IN_PROGRESS = new ValidationResult(Status.IN_PROGRESS);
    public static final ValidationResult CANCELLED = new ValidationResult(Status.CANCELLED);
    
    private Status status;
    
    enum Status { SUCCESS, INCORRECT, IN_PROGRESS, CANCELLED, INVALID }
}
```

### Scene3ViewModel.java (Cooking Logic)
```java
public class Scene3ViewModel extends AndroidViewModel {
    private MutableLiveData<CookingStep> currentStep = new MutableLiveData<>();
    private MutableLiveData<Integer> stepIndex = new MutableLiveData<>(0);
    private MutableLiveData<Float> holdProgress = new MutableLiveData<>(0f);
    private MutableLiveData<Boolean> cookingComplete = new MutableLiveData<>(false);
    
    private List<CookingStep> cookingSteps;
    private StepValidator validator;
    private int errorCount = 0;
    private Handler holdHandler = new Handler(Looper.getMainLooper());
    private Runnable holdRunnable;

    public void init(SceneScript script) {
        cookingSteps = script.getCookingSteps();
        validator = new StepValidator();
        loadStep(0);
    }

    private void loadStep(int index) {
        if (index < cookingSteps.size()) {
            stepIndex.setValue(index);
            currentStep.setValue(cookingSteps.get(index));
            errorCount = 0;
        } else {
            cookingComplete.setValue(true);
        }
    }

    public void onItemDroppedToZone(String itemId, String zoneId) {
        CookingStep step = currentStep.getValue();
        if (step == null) return;
        
        PlayerAction action = new PlayerAction(ActionType.DROP, itemId, zoneId);
        ValidationResult result = validator.validate(step, action);
        
        if (result == ValidationResult.SUCCESS) {
            // Play success sound, show green check, persist item in zone
            proceedToNextStep();
        } else {
            // Play error sound, shake zone, increment errorCount
            errorCount++;
            if (errorCount >= 3) {
                enableHintMode();
            }
        }
    }

    public void onHoldStart(String itemId, String zoneId) {
        holdProgress.setValue(0f);
        CookingStep step = currentStep.getValue();
        if (step == null || !step.getStepType().equals("HoldToPour")) return;
        
        final int requiredMs = step.getRequiredHoldMs();
        final long startTime = System.currentTimeMillis();
        
        holdRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, (float) elapsed / requiredMs);
                holdProgress.setValue(progress);
                
                if (progress >= 1f) {
                    onHoldComplete(itemId, zoneId, (int) elapsed);
                } else {
                    holdHandler.postDelayed(this, 50); // update every 50ms
                }
            }
        };
        holdHandler.post(holdRunnable);
    }

    public void onHoldCancel() {
        if (holdRunnable != null) {
            holdHandler.removeCallbacks(holdRunnable);
            holdRunnable = null;
        }
        holdProgress.setValue(0f);
        // Play cancel sound
    }

    private void onHoldComplete(String itemId, String zoneId, int duration) {
        holdHandler.removeCallbacks(holdRunnable);
        holdRunnable = null;
        
        PlayerAction action = new PlayerAction(ActionType.HOLD_COMPLETE, itemId, zoneId, duration);
        CookingStep step = currentStep.getValue();
        ValidationResult result = validator.validate(step, action);
        
        if (result == ValidationResult.SUCCESS) {
            proceedToNextStep();
        }
    }

    private void proceedToNextStep() {
        int current = stepIndex.getValue() != null ? stepIndex.getValue() : 0;
        loadStep(current + 1);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (holdHandler != null && holdRunnable != null) {
            holdHandler.removeCallbacks(holdRunnable);
        }
    }
}
```

---

## 6. XML LAYOUTS (Skeleton with IDs)

### activity_masterchef_map.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/map_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <!-- Background Parallax Layers (MotionLayout) -->
    <androidx.constraintlayout.motion.widget.MotionLayout
        android:id="@+id/map_parallax_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layoutDescription="@xml/scene_map_parallax">

        <ImageView
            android:id="@+id/bg_layer_far"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:src="@drawable/bg_map_far" />

        <ImageView
            android:id="@+id/bg_layer_mid"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:src="@drawable/bg_map_mid" />

        <ImageView
            android:id="@+id/bg_layer_near"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:src="@drawable/bg_map_near" />

    </androidx.constraintlayout.motion.widget.MotionLayout>

    <!-- Level Nodes ScrollView -->
    <HorizontalScrollView
        android:id="@+id/map_scroll"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="none">

        <LinearLayout
            android:id="@+id/level_nodes_container"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:orientation="horizontal"
            android:padding="32dp"
            android:gravity="center_vertical">
            
            <!-- Level nodes will be added programmatically -->
            
        </LinearLayout>

    </HorizontalScrollView>

    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@android:color/transparent"
        app:navigationIcon="@drawable/ic_back"
        app:title="Master Chef Map"
        app:titleTextColor="@color/white" />

    <!-- Cookbook FAB -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_cookbook"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="24dp"
        android:contentDescription="@string/cookbook"
        app:srcCompat="@drawable/ic_cookbook" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### fragment_scene1_ordering.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    android:clipChildren="false">

    <!-- Background -->
    <ImageView
        android:id="@+id/bg_restaurant"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scaleType="centerCrop"
        android:src="@drawable/bg_restaurant_interior"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Character (Player) -->
    <ImageView
        android:id="@+id/character_player"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/character_kid_neutral"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_marginStart="32dp"
        android:layout_marginBottom="64dp" />

    <!-- NPC (Chef/Waiter) -->
    <ImageView
        android:id="@+id/npc_chef"
        android:layout_width="140dp"
        android:layout_height="140dp"
        android:src="@drawable/npc_chef_neutral"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginEnd="32dp"
        android:layout_marginBottom="64dp" />

    <!-- Player Speech Bubble -->
    <androidx.cardview.widget.CardView
        android:id="@+id/bubble_player"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginEnd="24dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:layout_constraintBottom_toTopOf="@id/character_player"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/npc_chef"
        app:layout_constraintWidth_max="280dp">

        <TextView
            android:id="@+id/text_player_dialog"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp"
            android:textSize="16sp"
            android:textColor="@color/black"
            android:lineSpacingExtra="4dp" />

    </androidx.cardview.widget.CardView>

    <!-- NPC Speech Bubble -->
    <androidx.cardview.widget.CardView
        android:id="@+id/bubble_npc"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginEnd="24dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/yellow_light"
        app:layout_constraintBottom_toTopOf="@id/npc_chef"
        app:layout_constraintStart_toEndOf="@id/character_player"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintWidth_max="280dp">

        <TextView
            android:id="@+id/text_npc_dialog"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp"
            android:textSize="16sp"
            android:textColor="@color/black"
            android:lineSpacingExtra="4dp" />

    </androidx.cardview.widget.CardView>

    <!-- Menu Popup (Simple for MVP: just show dish name) -->
    <androidx.cardview.widget.CardView
        android:id="@+id/menu_card"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="32dp"
        android:layout_marginEnd="32dp"
        android:visibility="gone"
        app:cardCornerRadius="24dp"
        app:cardElevation="8dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp"
            android:gravity="center">

            <ImageView
                android:id="@+id/menu_dish_image"
                android:layout_width="200dp"
                android:layout_height="200dp"
                android:scaleType="centerCrop"
                android:src="@drawable/dish_sample" />

            <TextView
                android:id="@+id/menu_dish_name"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Spaghetti Bolognese"
                android:textSize="24sp"
                android:textStyle="bold"
                android:layout_marginTop="16dp" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_select_dish"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Order This!"
                android:layout_marginTop="16dp"
                app:cornerRadius="12dp" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Replay Audio Button -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_replay"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/replay"
        app:fabSize="mini"
        app:srcCompat="@drawable/ic_replay"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### fragment_scene2_shopping.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <!-- Background -->
    <ImageView
        android:id="@+id/bg_supermarket"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scaleType="centerCrop"
        android:src="@drawable/bg_supermarket"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Chef Request Bubble -->
    <androidx.cardview.widget.CardView
        android:id="@+id/bubble_chef_request"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/yellow_light"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="I need:"
                android:textSize="18sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/text_ingredient_list"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:textSize="16sp"
                android:lineSpacingExtra="4dp" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Ingredients Grid -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_ingredients"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="16dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="8dp"
        app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
        app:spanCount="3"
        app:layout_constraintTop_toBottomOf="@id/bubble_chef_request"
        app:layout_constraintBottom_toTopOf="@id/cart_container"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Cart Container -->
    <androidx.cardview.widget.CardView
        android:id="@+id/cart_container"
        android:layout_width="0dp"
        android:layout_height="120dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:layout_constraintBottom_toTopOf="@id/btn_submit_cart"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:padding="8dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="My Cart"
                android:textSize="14sp"
                android:textStyle="bold" />

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recycler_cart"
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:layout_marginTop="4dp"
                android:orientation="horizontal"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Submit Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_submit_cart"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="32dp"
        android:layout_marginEnd="32dp"
        android:layout_marginBottom="24dp"
        android:text="Submit Order"
        android:textSize="18sp"
        app:cornerRadius="12dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Replay FAB -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_replay"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/replay"
        app:fabSize="mini"
        app:srcCompat="@drawable/ic_replay"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### fragment_scene3_cooking.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <!-- Background -->
    <ImageView
        android:id="@+id/bg_kitchen"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scaleType="centerCrop"
        android:src="@drawable/bg_kitchen"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Step Instruction Card -->
    <androidx.cardview.widget.CardView
        android:id="@+id/card_instruction"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="@color/blue_light"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="12dp"
            android:gravity="center_vertical">

            <ImageView
                android:id="@+id/img_step_hint"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:src="@drawable/ic_hint_placeholder"
                android:scaleType="centerInside" />

            <TextView
                android:id="@+id/text_step_instruction"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="12dp"
                android:text="Drag tomatoes into the pot"
                android:textSize="16sp"
                android:textColor="@color/black" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Progress Bar -->
    <com.google.android.material.progressindicator.LinearProgressIndicator
        android:id="@+id/progress_steps"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/card_instruction"
        app:layout_constraintStart_toStartOf="@id/card_instruction"
        app:layout_constraintEnd_toEndOf="@id/card_instruction" />

    <!-- Kitchen Zones Container -->
    <FrameLayout
        android:id="@+id/kitchen_zones_container"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="8dp"
        app:layout_constraintTop_toBottomOf="@id/progress_steps"
        app:layout_constraintBottom_toTopOf="@id/ingredient_tray_scroll"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <!-- CUTTING ZONE -->
        <FrameLayout
            android:id="@+id/zone_cutting"
            android:layout_width="140dp"
            android:layout_height="140dp"
            android:layout_gravity="start|top"
            android:layout_marginStart="32dp"
            android:layout_marginTop="32dp"
            android:background="@drawable/bg_zone_cutting">
            
            <!-- Items will be added dynamically -->

        </FrameLayout>

        <!-- STOVE ZONE -->
        <FrameLayout
            android:id="@+id/zone_stove"
            android:layout_width="160dp"
            android:layout_height="160dp"
            android:layout_gravity="center_horizontal|top"
            android:layout_marginTop="16dp"
            android:background="@drawable/bg_zone_stove">

        </FrameLayout>

        <!-- OVEN ZONE -->
        <FrameLayout
            android:id="@+id/zone_oven"
            android:layout_width="140dp"
            android:layout_height="140dp"
            android:layout_gravity="end|top"
            android:layout_marginEnd="32dp"
            android:layout_marginTop="32dp"
            android:background="@drawable/bg_zone_oven">

        </FrameLayout>

        <!-- COUNTER ZONE -->
        <FrameLayout
            android:id="@+id/zone_counter"
            android:layout_width="200dp"
            android:layout_height="120dp"
            android:layout_gravity="start|bottom"
            android:layout_marginStart="24dp"
            android:layout_marginBottom="40dp"
            android:background="@drawable/bg_zone_counter">

        </FrameLayout>

        <!-- PLATE ZONE -->
        <FrameLayout
            android:id="@+id/zone_plate"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:layout_gravity="end|bottom"
            android:layout_marginEnd="40dp"
            android:layout_marginBottom="40dp"
            android:background="@drawable/bg_zone_plate">

        </FrameLayout>

    </FrameLayout>

    <!-- Ingredient/Tool Tray -->
    <HorizontalScrollView
        android:id="@+id/ingredient_tray_scroll"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:layout_marginBottom="16dp"
        android:background="@drawable/bg_tray"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:id="@+id/ingredient_tray"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:orientation="horizontal"
            android:padding="8dp"
            android:gravity="center_vertical">

            <!-- Items will be added dynamically -->

        </LinearLayout>

    </HorizontalScrollView>

    <!-- Hold Progress Overlay (for pour/stir gestures) -->
    <androidx.cardview.widget.CardView
        android:id="@+id/hold_progress_overlay"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:cardCornerRadius="60dp"
        app:cardElevation="8dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.progressindicator.CircularProgressIndicator
            android:id="@+id/circular_hold_progress"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:layout_margin="16dp"
            app:indicatorSize="120dp"
            app:trackThickness="12dp" />

    </androidx.cardview.widget.CardView>

    <!-- Replay FAB -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_replay"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/replay"
        app:fabSize="mini"
        app:srcCompat="@drawable/ic_replay"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### activity_cookbook.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <!-- Toolbar -->
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:navigationIcon="@drawable/ic_back"
            app:title="My Cookbook" />

    </com.google.android.material.appbar.AppBarLayout>

    <!-- ViewPager2 -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewpager_cookbook"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="?attr/actionBarSize"
        android:padding="16dp"
        android:clipToPadding="false" />

    <!-- Page Indicator -->
    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tab_indicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center_horizontal"
        android:layout_marginBottom="24dp"
        app:tabBackground="@drawable/tab_selector"
        app:tabGravity="center"
        app:tabIndicatorHeight="0dp" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

---

## 7. ASSET & ANIMATION REQUIREMENTS

### Drawables (XML + PNG/Vector)
**Backgrounds:**
- `bg_map_far.png` (2048x1024, parallax far layer)
- `bg_map_mid.png` (2048x1024, parallax mid layer)
- `bg_map_near.png` (2048x1024, parallax near layer)
- `bg_restaurant_interior.png` (1920x1080)
- `bg_supermarket.png` (1920x1080)
- `bg_kitchen.png` (1920x1080)

**Characters:**
- `character_kid_neutral.png` (512x512, transparent)
- `npc_chef_neutral.png` (512x512, transparent)

**Zones (XML drawables with stroke/fill):**
- `bg_zone_cutting.xml` (rounded rect, stroke blue)
- `bg_zone_stove.xml` (rounded rect, stroke red)
- `bg_zone_oven.xml` (rounded rect, stroke orange)
- `bg_zone_counter.xml` (rounded rect, stroke gray)
- `bg_zone_plate.xml` (circle, stroke green)
- `bg_tray.xml` (rounded rect, fill light gray)

**Icons/Items (PNG 128x128):**
- Ingredients: `ing_tomato.png`, `ing_onion.png`, `ing_garlic.png`, `ing_meat.png`, `ing_pasta.png`, `ing_cheese.png`, `ing_oil.png`, `ing_salt.png`, etc.
- Tools: `tool_knife.png`, `tool_pan.png`, `tool_pot.png`, `tool_bowl.png`, `tool_spatula.png`, `tool_ladle.png`, `tool_whisk.png`
- Dishes: `dish_spaghetti.png`, `dish_pizza.png`, `dish_burger.png`

**UI Icons (Vector):**
- `ic_back.xml`
- `ic_cookbook.xml`
- `ic_replay.xml`
- `ic_hint_placeholder.xml`
- `ic_lock.xml` (for locked levels)
- `ic_star.xml` (for rating)

**Lottie Animations:**
- `lottie_sparkle.json` (success effect)
- `lottie_lock_shake.json` (locked level shake)
- `lottie_pulse.json` (hint pulse)

### MotionLayout Scenes
**`scene_map_parallax.xml`** (res/xml)
```xml
<MotionScene>
    <Transition
        motion:constraintSetStart="@id/start"
        motion:constraintSetEnd="@id/end"
        motion:duration="1000" />

    <ConstraintSet android:id="@+id/start">
        <!-- Define initial parallax positions -->
    </ConstraintSet>

    <ConstraintSet android:id="@+id/end">
        <!-- Define end parallax positions (camera moved right) -->
    </ConstraintSet>
</MotionScene>
```

### Animations (res/anim)
- `fade_in.xml`
- `fade_out.xml`
- `slide_up.xml`
- `slide_down.xml`
- `shake.xml` (for error feedback)
- `bounce.xml` (for menu appearance)

---

## 8. EXPANSION STRATEGY (DATA-DRIVEN)

### Adding New Levels (NO CODE CHANGE)
1. Create new `LevelConfig` JSON or Java object:
```json
{
  "levelId": 5,
  "dishId": "pizza_margherita",
  "difficulty": "normal",
  "unlockCondition": 4,
  "star1Threshold": 50,
  "star2Threshold": 80,
  "star3Threshold": 100,
  "scene1Script": {
    "dialogLines": [
      {"speaker": "npc", "text": "Welcome! What would you like?"},
      {"speaker": "player", "text": "I want to order Pizza Margherita."},
      {"speaker": "npc", "text": "Great choice! Please wait a moment."}
    ]
  },
  "scene2Script": {
    "requiredIngredientIds": ["flour", "tomato", "mozzarella", "basil"],
    "distractorIngredientIds": ["pepper", "mushroom"]
  },
  "scene3Script": {
    "cookingSteps": [
      {"stepType": "DragDropToZone", "itemId": "flour", "targetZone": "zone_counter", "instructionText": "Place flour on counter"},
      {"stepType": "TapOnItem", "itemId": "flour", "requiredCount": 5, "instructionText": "Tap to knead dough"},
      {"stepType": "DragDropToZone", "itemId": "tomato", "targetZone": "zone_cutting", "instructionText": "Place tomato on cutting board"},
      {"stepType": "TapOnZone", "zoneId": "zone_cutting", "requiredCount": 3, "instructionText": "Tap to chop tomato"},
      {"stepType": "DragDropToZone", "itemId": "dough", "targetZone": "zone_oven", "instructionText": "Place dough in oven"},
      {"stepType": "TimerWait", "seconds": 5, "instructionText": "Wait for baking..."},
      {"stepType": "PlateServe", "itemId": "pizza", "targetZone": "zone_plate", "instructionText": "Serve on plate"}
    ]
  }
}
```

2. Add to `LevelRepository` data source (local JSON file or hardcoded list)
3. Add dish assets (image + ingredient icons)
4. Done! Core logic handles all validation/flow.

### Adding New Step Types (MINIMAL CODE)
1. Add new `stepType` string (e.g., "MixGesture")
2. Implement validation in `StepValidator.java`:
```java
case "MixGesture":
    return validateMixGesture(step, action);
```
3. Add UI handler in `Scene3Fragment.java`
4. No changes to other scenes or state machine

---

## 9. COMMON PITFALLS & FIXES CHECKLIST

### ✅ Notch/Cutout Handling
- ✅ Use `android:fitsSystemWindows="true"` on root layouts
- ✅ Use `CoordinatorLayout` + `AppBarLayout` for proper insets
- ✅ Test on devices with notch (e.g., Pixel 5, Samsung S20+)
- ✅ Check WindowInsetsCompat for bottom nav bar too

### ✅ Back Navigation
- ✅ Override `onBackPressed()` in all activities:
```java
@Override
public void onBackPressed() {
    if (currentScene == Scene.COOKING && hasUnsavedProgress) {
        showExitConfirmDialog();
    } else {
        super.onBackPressed();
    }
}
```
- ✅ Handle back in fragments: set `OnBackPressedCallback`
- ✅ Save progress before navigating back

### ✅ Bubble ClipChildren
- ✅ Set `android:clipChildren="false"` on parent ViewGroup
- ✅ Set `android:clipToPadding="false"` if scrollable
- ✅ Use `android:elevation` or `app:cardElevation` to ensure bubble renders above siblings

### ✅ Gesture Stability (Hold/Cancel)
- ✅ **Remove all Handler callbacks in `onPause()`, `onStop()`, `onCleared()`**
- ✅ Cancel hold when:
  - User lifts finger (ACTION_UP/ACTION_CANCEL)
  - Activity pauses (lifecycle)
  - Fragment detaches
- ✅ Use `Looper.getMainLooper()` for Handler, NOT background thread
- ✅ Clamp progress: `Math.min(1f, progress)`
- ✅ Null-check `holdRunnable` before removing callbacks

### ✅ Persist Items After Drop
- ✅ When item dropped successfully:
  1. Remove from tray
  2. Add ImageView to target zone FrameLayout
  3. Store in ViewModel state (e.g., `zoneItems: Map<String, List<String>>`)
  4. On step change, re-render zone contents from state
- ✅ Use `ViewGroup.addView(view)` to add item to zone
- ✅ Set `android:elevation` or `translationZ` to layer items correctly

### ✅ TTS Karaoke Sync
- ✅ Use `UtteranceProgressListener` to track word boundaries (if supported)
- ✅ OR: manually split text + estimate timing (e.g., 300ms per word)
- ✅ Highlight with SpannableString + ForegroundColorSpan
- ✅ Update TextView each 100ms with new span
- ✅ Handle TTS initialization failure gracefully (show text without highlight)

### ✅ SoundPool & Memory
- ✅ Preload all sounds in `onCreate()` (don't load during gameplay)
- ✅ Release SoundPool in `onDestroy()`
- ✅ Use `MediaPlayer` for long BGM, `SoundPool` for short SFX
- ✅ Pause/resume BGM in `onPause()`/`onResume()`

### ✅ Animation Performance
- ✅ Use `ObjectAnimator` instead of `ViewPropertyAnimator` for complex chains
- ✅ Avoid animating `layout_width`/`layout_height` (use `scaleX`/`scaleY`)
- ✅ Use `setLayerType(LAYER_TYPE_HARDWARE, null)` during animation
- ✅ Restore layer type after animation: `setLayerType(LAYER_TYPE_NONE, null)`

### ✅ Data Persistence
- ✅ Save progress IMMEDIATELY after level complete (don't wait for app close)
- ✅ Use `SharedPreferences.Editor.apply()` for async writes
- ✅ OR: Use Room for structured data (recommended for 10+ levels)
- ✅ Save unlocked levels, stars, cookbook entries

### ✅ Testing on Real Devices
- ✅ Test on low-end device (2GB RAM, Android 8)
- ✅ Test on high-end device with notch (Android 12+)
- ✅ Test landscape orientation (lock to portrait if not supported)
- ✅ Test with TalkBack enabled (accessibility labels)
- ✅ Test with device in Do Not Disturb mode (sounds muted)

---

## 10. NEXT STEPS (Implementation Order)

1. ✅ Create data models (Food, Ingredient, LevelConfig, etc.)
2. ✅ Create mock data (1 level: Spaghetti Bolognese)
3. ✅ Implement MasterChefMapActivity + LevelNodeView
4. ✅ Implement Scene1Fragment + ViewModel (ordering)
5. ✅ Implement TTSManager + KaraokeHelper
6. ✅ Implement Scene2Fragment + ViewModel (shopping)
7. ✅ Implement Scene3Fragment + ViewModel (cooking, start with DragDrop only)
8. ✅ Add more step types (Hold, Tap, Stir, etc.)
9. ✅ Implement scoring + stars + LevelCompleteDialog
10. ✅ Implement CookbookActivity
11. ✅ Add SoundPoolManager + animations
12. ✅ Polish UI/UX (transitions, feedback, hints)
13. ✅ Test on devices
14. ✅ Add more levels (data-driven)

---

## NOTES
- Start with **1 complete level (Spaghetti Bolognese)** as MVP
- Test each scene independently before integration
- Prioritize stable gesture handling (Hold is the most complex)
- Use placeholder assets initially (colored rectangles), replace later
- Document all public APIs for ViewModels (for future expansion)

---

END OF DESIGN DOCUMENT
