package com.edu.english.magicmelody.ui.boss;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.repository.AssetDataRepository;
import com.edu.english.magicmelody.data.repository.BossProgressRepository;
import com.edu.english.magicmelody.data.repository.CollectedNoteRepository;
import com.edu.english.magicmelody.data.repository.UserProfileRepository;
import com.edu.english.magicmelody.data.repository.WorldProgressRepository;
import com.edu.english.magicmelody.model.ThemeConfig;
import com.edu.english.magicmelody.util.Constants;

import java.util.List;

/**
 * 👹 Boss Battle ViewModel
 * 
 * Purpose: Handle boss battle (AR concert) logic
 * - Boss health & attacks
 * - Voice detection for attacks
 * - Camera/AR overlay
 * - Victory conditions
 */
public class BossBattleViewModel extends AndroidViewModel {
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    private final BossProgressRepository bossProgressRepository;
    private final WorldProgressRepository worldProgressRepository;
    private final CollectedNoteRepository collectedNoteRepository;
    private final UserProfileRepository userProfileRepository;
    private final AssetDataRepository assetDataRepository;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LIVE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private final MutableLiveData<BattleState> battleState = new MutableLiveData<>(BattleState.INTRO);
    private final MutableLiveData<BossInfo> bossInfo = new MutableLiveData<>();
    private final MutableLiveData<Integer> bossHealth = new MutableLiveData<>(100);
    private final MutableLiveData<Integer> playerHealth = new MutableLiveData<>(100);
    private final MutableLiveData<String> currentDialogue = new MutableLiveData<>();
    private final MutableLiveData<AttackEvent> attackEvent = new MutableLiveData<>();
    private final MutableLiveData<VoiceDetectionState> voiceState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isArModeActive = new MutableLiveData<>(false);
    private final MutableLiveData<BattleResult> battleResult = new MutableLiveData<>();
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 BATTLE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private long userId = -1;
    private String bossId;
    private String worldId;
    private ThemeConfig.BossConfig bossConfig;
    private int maxBossHealth = 100;
    private int totalVoiceAttacks = 0;
    private long battleStartTime = 0;
    private boolean usedArMode = false;
    
    private Handler battleHandler;
    private boolean isBattleRunning = false;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ENUMS & EVENTS
    // ═══════════════════════════════════════════════════════════════
    
    public enum BattleState {
        INTRO,          // Boss introduction
        COUNTDOWN,      // 3-2-1 countdown
        FIGHTING,       // Active battle
        BOSS_ATTACK,    // Boss is attacking
        PLAYER_ATTACK,  // Player attacking with voice
        VICTORY,        // Player won
        DEFEAT,         // Player lost
        PAUSED
    }
    
    public static class BossInfo {
        public final String bossId;
        public final String name;
        public final int maxHealth;
        public final List<String> weaknesses;
        public final String modelFile;
        
        public BossInfo(ThemeConfig.BossConfig config) {
            this.bossId = config.getId();
            this.name = config.getName();
            this.maxHealth = config.getHealth();
            this.weaknesses = config.getWeakness();
            this.modelFile = config.getModelFile();
        }
    }
    
    public static class AttackEvent {
        public enum Type {
            BOSS_ATTACK,
            PLAYER_VOICE_ATTACK,
            PLAYER_NOTE_ATTACK,
            CRITICAL_HIT,
            MISS
        }
        
        public final Type type;
        public final int damage;
        public final String noteName;
        
        public AttackEvent(Type type, int damage, String noteName) {
            this.type = type;
            this.damage = damage;
            this.noteName = noteName;
        }
    }
    
    public static class VoiceDetectionState {
        public final boolean isListening;
        public final float volumeLevel;
        public final String detectedNote;
        
        public VoiceDetectionState(boolean isListening, float volumeLevel, String detectedNote) {
            this.isListening = isListening;
            this.volumeLevel = volumeLevel;
            this.detectedNote = detectedNote;
        }
    }
    
    public static class BattleResult {
        public final boolean victory;
        public final boolean perfectVictory;
        public final int score;
        public final long timeMs;
        public final int voiceAttacksUsed;
        public final String rewardNoteId;
        
        public BattleResult(boolean victory, boolean perfectVictory, int score, 
                           long timeMs, int voiceAttacks, String rewardNote) {
            this.victory = victory;
            this.perfectVictory = perfectVictory;
            this.score = score;
            this.timeMs = timeMs;
            this.voiceAttacksUsed = voiceAttacks;
            this.rewardNoteId = rewardNote;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public BossBattleViewModel(@NonNull Application application) {
        super(application);
        
        bossProgressRepository = new BossProgressRepository(application);
        worldProgressRepository = new WorldProgressRepository(application);
        collectedNoteRepository = new CollectedNoteRepository(application);
        userProfileRepository = new UserProfileRepository(application);
        assetDataRepository = new AssetDataRepository(application);
        
        battleHandler = new Handler(Looper.getMainLooper());
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<BattleState> getBattleState() {
        return battleState;
    }
    
    public LiveData<BossInfo> getBossInfo() {
        return bossInfo;
    }
    
    public LiveData<Integer> getBossHealth() {
        return bossHealth;
    }
    
    public LiveData<Integer> getPlayerHealth() {
        return playerHealth;
    }
    
    public LiveData<String> getCurrentDialogue() {
        return currentDialogue;
    }
    
    public LiveData<AttackEvent> getAttackEvent() {
        return attackEvent;
    }
    
    public LiveData<VoiceDetectionState> getVoiceState() {
        return voiceState;
    }
    
    public LiveData<Boolean> getIsArModeActive() {
        return isArModeActive;
    }
    
    public LiveData<BattleResult> getBattleResult() {
        return battleResult;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 BATTLE LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize boss battle
     */
    public void initialize(String worldId, long userId) {
        this.worldId = worldId;
        this.userId = userId;
        
        // Load boss config from theme
        ThemeConfig theme = assetDataRepository.getTheme(worldId);
        if (theme == null || theme.getBoss() == null) {
            // Handle error
            return;
        }
        
        bossConfig = theme.getBoss();
        bossId = bossConfig.getId();
        maxBossHealth = bossConfig.getHealth();
        
        // Initialize boss progress if not exists
        bossProgressRepository.initializeBoss(bossId, bossConfig.getName(), worldId, userId);
        
        // Set initial state
        bossHealth.setValue(maxBossHealth);
        playerHealth.setValue(100);
        bossInfo.setValue(new BossInfo(bossConfig));
        
        // Show intro dialogue
        battleState.setValue(BattleState.INTRO);
        currentDialogue.setValue(bossConfig.getDialogue().getIntro());
    }
    
    /**
     * Start battle after intro
     */
    public void startBattle() {
        battleState.setValue(BattleState.COUNTDOWN);
        
        // Countdown
        battleHandler.postDelayed(() -> {
            battleState.setValue(BattleState.FIGHTING);
            battleStartTime = System.currentTimeMillis();
            isBattleRunning = true;
            startBossAI();
        }, 3000);
    }
    
    /**
     * Start boss AI attacks
     */
    private void startBossAI() {
        Runnable bossAttackRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isBattleRunning) return;
                if (battleState.getValue() != BattleState.FIGHTING) return;
                
                // Boss attacks periodically
                performBossAttack();
                
                // Schedule next attack (random interval)
                long nextAttackDelay = 3000 + (long)(Math.random() * 2000);
                battleHandler.postDelayed(this, nextAttackDelay);
            }
        };
        
        // First attack after delay
        battleHandler.postDelayed(bossAttackRunnable, 4000);
    }
    
    /**
     * Boss performs attack
     */
    private void performBossAttack() {
        battleState.setValue(BattleState.BOSS_ATTACK);
        
        int damage = 10 + (int)(Math.random() * 10);
        
        attackEvent.setValue(new AttackEvent(AttackEvent.Type.BOSS_ATTACK, damage, null));
        currentDialogue.setValue(bossConfig.getDialogue().getDamage());
        
        // Apply damage to player
        Integer currentPlayerHealth = playerHealth.getValue();
        if (currentPlayerHealth != null) {
            int newHealth = Math.max(0, currentPlayerHealth - damage);
            playerHealth.setValue(newHealth);
            
            if (newHealth <= 0) {
                endBattle(false);
                return;
            }
        }
        
        // Return to fighting state
        battleHandler.postDelayed(() -> {
            if (battleState.getValue() == BattleState.BOSS_ATTACK) {
                battleState.setValue(BattleState.FIGHTING);
            }
        }, 1000);
    }
    
    /**
     * Player attacks with voice/note
     */
    public void playerAttack(String noteName, float intensity) {
        if (battleState.getValue() != BattleState.FIGHTING) return;
        
        totalVoiceAttacks++;
        battleState.setValue(BattleState.PLAYER_ATTACK);
        
        // Calculate damage
        int baseDamage = (int)(10 * intensity);
        boolean isCritical = bossConfig.isWeakTo(noteName);
        
        if (isCritical) {
            baseDamage *= 2; // Double damage for weakness
        }
        
        AttackEvent.Type attackType = isCritical 
            ? AttackEvent.Type.CRITICAL_HIT 
            : AttackEvent.Type.PLAYER_VOICE_ATTACK;
        
        attackEvent.setValue(new AttackEvent(attackType, baseDamage, noteName));
        
        // Apply damage to boss
        Integer currentBossHealth = bossHealth.getValue();
        if (currentBossHealth != null) {
            int newHealth = Math.max(0, currentBossHealth - baseDamage);
            bossHealth.setValue(newHealth);
            
            if (newHealth <= 0) {
                currentDialogue.setValue(bossConfig.getDialogue().getDefeat());
                endBattle(true);
                return;
            }
        }
        
        // Return to fighting state
        battleHandler.postDelayed(() -> {
            if (battleState.getValue() == BattleState.PLAYER_ATTACK) {
                battleState.setValue(BattleState.FIGHTING);
            }
        }, 500);
    }
    
    /**
     * Update voice detection state
     */
    public void onVoiceDetected(float volumeLevel, String detectedNote) {
        voiceState.setValue(new VoiceDetectionState(true, volumeLevel, detectedNote));
        
        // If volume is high enough and note detected, attack
        if (volumeLevel > 0.5f && detectedNote != null) {
            playerAttack(detectedNote, volumeLevel);
        }
    }
    
    /**
     * Toggle AR mode
     */
    public void toggleArMode() {
        Boolean current = isArModeActive.getValue();
        isArModeActive.setValue(current == null || !current);
        
        if (Boolean.TRUE.equals(isArModeActive.getValue())) {
            usedArMode = true;
        }
    }
    
    /**
     * End battle
     */
    private void endBattle(boolean victory) {
        isBattleRunning = false;
        battleHandler.removeCallbacksAndMessages(null);
        
        battleState.setValue(victory ? BattleState.VICTORY : BattleState.DEFEAT);
        
        long battleTime = System.currentTimeMillis() - battleStartTime;
        Integer finalPlayerHealth = playerHealth.getValue();
        boolean perfectVictory = victory && (finalPlayerHealth != null && finalPlayerHealth >= 100);
        
        int score = calculateScore(victory, perfectVictory, battleTime);
        
        // Record battle result
        bossProgressRepository.recordBattleAttempt(
            bossId, userId, score, battleTime, usedArMode, totalVoiceAttacks,
            victory, perfectVictory,
            (v, pv) -> {
                if (victory) {
                    // Mark world boss as defeated
                    worldProgressRepository.markBossDefeated(worldId, userId);
                    
                    // Award legendary note
                    String rewardNoteId = "legendary_" + bossId;
                    collectedNoteRepository.collectNote(
                        bossConfig.getName() + "'s Melody",
                        "Victory!",
                        "legendary",
                        worldId,
                        bossId,
                        userId,
                        null
                    );
                    
                    battleResult.postValue(new BattleResult(
                        true, perfectVictory, score, battleTime, totalVoiceAttacks, rewardNoteId
                    ));
                } else {
                    battleResult.postValue(new BattleResult(
                        false, false, score, battleTime, totalVoiceAttacks, null
                    ));
                }
            }
        );
    }
    
    /**
     * Calculate battle score
     */
    private int calculateScore(boolean victory, boolean perfect, long timeMs) {
        int base = victory ? 1000 : 100;
        
        if (perfect) base += 500;
        
        // Time bonus (faster = better)
        if (timeMs < 60000) { // Under 1 minute
            base += 300;
        } else if (timeMs < 120000) { // Under 2 minutes
            base += 100;
        }
        
        return base;
    }
    
    /**
     * Pause battle
     */
    public void pauseBattle() {
        if (battleState.getValue() == BattleState.FIGHTING) {
            battleState.setValue(BattleState.PAUSED);
        }
    }
    
    /**
     * Resume battle
     */
    public void resumeBattle() {
        if (battleState.getValue() == BattleState.PAUSED) {
            battleState.setValue(BattleState.FIGHTING);
        }
    }
    
    /**
     * Quit battle
     */
    public void quitBattle() {
        isBattleRunning = false;
        battleHandler.removeCallbacksAndMessages(null);
    }
    
    /**
     * Retry battle
     */
    public void retryBattle() {
        totalVoiceAttacks = 0;
        usedArMode = false;
        initialize(worldId, userId);
        startBattle();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🧹 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onCleared() {
        super.onCleared();
        isBattleRunning = false;
        if (battleHandler != null) {
            battleHandler.removeCallbacksAndMessages(null);
        }
    }
}
