package com.edu.english.magicmelody.gameplay.boss;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 👹 Boss Battle Engine
 * 
 * Manages boss fight mechanics:
 * - Boss AI with attack patterns
 * - Health and damage system
 * - Battle phases
 * - Player vs Boss interactions
 */
public class BossBattleEngine {
    
    private static final String TAG = "BossBattleEngine";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ENUMS
    // ═══════════════════════════════════════════════════════════════
    
    public enum BattleState {
        IDLE,
        INTRO,           // Boss introduction
        PLAYER_TURN,     // Player attacking
        BOSS_TURN,       // Boss attacking
        PLAYER_DEFEND,   // Player must defend
        VICTORY,         // Player won
        DEFEAT           // Player lost
    }
    
    public enum BossPhase {
        PHASE_1(1.0f, "Normal"),      // 100% - 60% health
        PHASE_2(1.3f, "Angry"),       // 60% - 30% health
        PHASE_3(1.6f, "Enraged");     // 30% - 0% health
        
        private final float difficultyMultiplier;
        private final String displayName;
        
        BossPhase(float multiplier, String name) {
            this.difficultyMultiplier = multiplier;
            this.displayName = name;
        }
        
        public float getDifficultyMultiplier() { return difficultyMultiplier; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum AttackType {
        WORD_SPELL,      // Player must spell a word
        VOICE_ATTACK,    // Player must speak a word
        RHYTHM_BLAST,    // Rhythm mini-game
        QUICK_TAP,       // Quick tap challenge
        SHIELD_BREAK     // Break boss shield
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 BOSS DATA
    // ═══════════════════════════════════════════════════════════════
    
    public static class BossData {
        public String bossId;
        public String bossName;
        public String worldId;
        public int maxHealth = 100;
        public int baseDamage = 10;
        public List<AttackPattern> attackPatterns = new ArrayList<>();
        public List<String> weaknessWords = new ArrayList<>();  // Words boss is weak to
        public String victoryReward;
        
        public static BossData createDefault(String worldId) {
            BossData data = new BossData();
            data.bossId = "boss_" + worldId;
            data.bossName = "Shadow Guardian";
            data.worldId = worldId;
            data.maxHealth = 100;
            data.baseDamage = 10;
            
            // Default attack patterns
            data.attackPatterns.add(new AttackPattern(AttackType.WORD_SPELL, 0.4f));
            data.attackPatterns.add(new AttackPattern(AttackType.VOICE_ATTACK, 0.3f));
            data.attackPatterns.add(new AttackPattern(AttackType.RHYTHM_BLAST, 0.2f));
            data.attackPatterns.add(new AttackPattern(AttackType.QUICK_TAP, 0.1f));
            
            return data;
        }
    }
    
    public static class AttackPattern {
        public AttackType type;
        public float probability;
        public int damage = 15;
        public long durationMs = 5000;
        public String[] words;
        
        public AttackPattern(AttackType type, float probability) {
            this.type = type;
            this.probability = probability;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 BATTLE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private BossData bossData;
    private BattleState currentState = BattleState.IDLE;
    private BossPhase currentPhase = BossPhase.PHASE_1;
    
    // Health
    private int bossCurrentHealth;
    private int playerCurrentHealth;
    private int playerMaxHealth = 100;
    
    // Combat
    private AttackPattern currentAttack;
    private int comboCount = 0;
    private int turnCount = 0;
    private long attackStartTimeMs;
    
    // Timing
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface BossBattleListener {
        void onBattleStateChanged(BattleState newState);
        void onPhaseChanged(BossPhase newPhase);
        void onBossHealthChanged(int currentHealth, int maxHealth);
        void onPlayerHealthChanged(int currentHealth, int maxHealth);
        void onBossAttack(AttackPattern attack);
        void onPlayerAttackResult(boolean success, int damage, String word);
        void onPlayerDefendResult(boolean success, int damageBlocked);
        void onComboChanged(int combo);
        void onVictory(int score, List<String> rewards);
        void onDefeat();
    }
    
    private BossBattleListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public BossBattleEngine() {
    }
    
    public BossBattleEngine(BossData bossData) {
        this.bossData = bossData;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(BossBattleListener listener) {
        this.listener = listener;
    }
    
    public void setBossData(BossData bossData) {
        this.bossData = bossData;
    }
    
    public void setPlayerMaxHealth(int maxHealth) {
        this.playerMaxHealth = maxHealth;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 BATTLE FLOW
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize and start battle
     */
    public void startBattle() {
        if (bossData == null) {
            throw new IllegalStateException("BossData not set");
        }
        
        // Reset state
        bossCurrentHealth = bossData.maxHealth;
        playerCurrentHealth = playerMaxHealth;
        currentPhase = BossPhase.PHASE_1;
        comboCount = 0;
        turnCount = 0;
        
        // Start with intro
        setState(BattleState.INTRO);
        
        // After intro, start player turn
        handler.postDelayed(() -> {
            startPlayerTurn();
        }, 3000);
    }
    
    /**
     * Start player's turn
     */
    public void startPlayerTurn() {
        turnCount++;
        setState(BattleState.PLAYER_TURN);
        
        // Player has limited time to attack
        handler.postDelayed(() -> {
            if (currentState == BattleState.PLAYER_TURN) {
                // Player took too long - boss attacks
                startBossTurn();
            }
        }, 10000);
    }
    
    /**
     * Player performs attack with word
     */
    public void playerAttack(String word, boolean correct) {
        if (currentState != BattleState.PLAYER_TURN) return;
        
        handler.removeCallbacksAndMessages(null);
        
        if (correct) {
            // Calculate damage
            int baseDamage = 10;
            
            // Combo bonus
            comboCount++;
            float comboMultiplier = 1.0f + (comboCount * 0.1f);
            
            // Weakness bonus
            boolean isWeakness = bossData.weaknessWords.contains(word.toLowerCase());
            float weaknessMultiplier = isWeakness ? 2.0f : 1.0f;
            
            int totalDamage = (int)(baseDamage * comboMultiplier * weaknessMultiplier);
            
            // Deal damage
            dealDamageToBoss(totalDamage);
            
            if (listener != null) {
                listener.onPlayerAttackResult(true, totalDamage, word);
                listener.onComboChanged(comboCount);
            }
            
            // Check victory
            if (bossCurrentHealth <= 0) {
                victory();
            } else {
                // Continue or boss turn
                if (comboCount >= 3) {
                    // Bonus attack after combo
                    handler.postDelayed(this::startPlayerTurn, 1000);
                } else {
                    handler.postDelayed(this::startBossTurn, 1500);
                }
            }
        } else {
            // Miss - reset combo
            comboCount = 0;
            
            if (listener != null) {
                listener.onPlayerAttackResult(false, 0, word);
                listener.onComboChanged(0);
            }
            
            // Boss counter-attacks
            handler.postDelayed(this::startBossTurn, 1000);
        }
    }
    
    /**
     * Start boss's turn
     */
    public void startBossTurn() {
        setState(BattleState.BOSS_TURN);
        
        // Select attack pattern
        currentAttack = selectAttackPattern();
        
        // Apply phase difficulty
        int adjustedDamage = (int)(currentAttack.damage * currentPhase.getDifficultyMultiplier());
        
        if (listener != null) {
            listener.onBossAttack(currentAttack);
        }
        
        // Switch to defend state
        handler.postDelayed(() -> {
            setState(BattleState.PLAYER_DEFEND);
            attackStartTimeMs = System.currentTimeMillis();
            
            // Player has limited time to defend
            handler.postDelayed(() -> {
                if (currentState == BattleState.PLAYER_DEFEND) {
                    // Failed to defend
                    playerDefend(false, null);
                }
            }, currentAttack.durationMs);
        }, 1500);
    }
    
    /**
     * Player defends against boss attack
     */
    public void playerDefend(boolean success, String wordUsed) {
        if (currentState != BattleState.PLAYER_DEFEND) return;
        
        handler.removeCallbacksAndMessages(null);
        
        int damage = (int)(currentAttack.damage * currentPhase.getDifficultyMultiplier());
        
        if (success) {
            // Blocked attack
            if (listener != null) {
                listener.onPlayerDefendResult(true, damage);
            }
        } else {
            // Take damage
            dealDamageToPlayer(damage);
            
            if (listener != null) {
                listener.onPlayerDefendResult(false, 0);
            }
            
            // Check defeat
            if (playerCurrentHealth <= 0) {
                defeat();
                return;
            }
        }
        
        // Next turn
        handler.postDelayed(this::startPlayerTurn, 1500);
    }
    
    /**
     * Select attack pattern based on probabilities and phase
     */
    private AttackPattern selectAttackPattern() {
        float roll = random.nextFloat();
        float cumulative = 0;
        
        for (AttackPattern pattern : bossData.attackPatterns) {
            cumulative += pattern.probability;
            if (roll <= cumulative) {
                return pattern;
            }
        }
        
        // Default to first pattern
        return bossData.attackPatterns.get(0);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 💔 DAMAGE SYSTEM
    // ═══════════════════════════════════════════════════════════════
    
    private void dealDamageToBoss(int damage) {
        bossCurrentHealth = Math.max(0, bossCurrentHealth - damage);
        
        if (listener != null) {
            listener.onBossHealthChanged(bossCurrentHealth, bossData.maxHealth);
        }
        
        // Check phase transition
        checkPhaseTransition();
    }
    
    private void dealDamageToPlayer(int damage) {
        playerCurrentHealth = Math.max(0, playerCurrentHealth - damage);
        
        if (listener != null) {
            listener.onPlayerHealthChanged(playerCurrentHealth, playerMaxHealth);
        }
    }
    
    private void checkPhaseTransition() {
        float healthPercent = (float) bossCurrentHealth / bossData.maxHealth;
        
        BossPhase newPhase;
        if (healthPercent > 0.6f) {
            newPhase = BossPhase.PHASE_1;
        } else if (healthPercent > 0.3f) {
            newPhase = BossPhase.PHASE_2;
        } else {
            newPhase = BossPhase.PHASE_3;
        }
        
        if (newPhase != currentPhase) {
            currentPhase = newPhase;
            if (listener != null) {
                listener.onPhaseChanged(currentPhase);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏆 VICTORY / DEFEAT
    // ═══════════════════════════════════════════════════════════════
    
    private void victory() {
        setState(BattleState.VICTORY);
        handler.removeCallbacksAndMessages(null);
        
        // Calculate rewards
        List<String> rewards = new ArrayList<>();
        rewards.add(bossData.victoryReward);
        
        // Bonus for remaining health
        if (playerCurrentHealth >= playerMaxHealth * 0.8f) {
            rewards.add("perfect_victory_badge");
        }
        
        // Calculate score
        int score = calculateVictoryScore();
        
        if (listener != null) {
            listener.onVictory(score, rewards);
        }
    }
    
    private void defeat() {
        setState(BattleState.DEFEAT);
        handler.removeCallbacksAndMessages(null);
        
        if (listener != null) {
            listener.onDefeat();
        }
    }
    
    private int calculateVictoryScore() {
        int score = 1000; // Base score
        
        // Health bonus
        score += (playerCurrentHealth * 5);
        
        // Turn efficiency bonus
        score += Math.max(0, (20 - turnCount) * 50);
        
        // Combo bonus
        score += comboCount * 100;
        
        return score;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    private void setState(BattleState newState) {
        currentState = newState;
        if (listener != null) {
            listener.onBattleStateChanged(newState);
        }
    }
    
    public BattleState getCurrentState() { return currentState; }
    public BossPhase getCurrentPhase() { return currentPhase; }
    public int getBossCurrentHealth() { return bossCurrentHealth; }
    public int getPlayerCurrentHealth() { return playerCurrentHealth; }
    public int getComboCount() { return comboCount; }
    public AttackPattern getCurrentAttack() { return currentAttack; }
    
    public float getBossHealthPercent() {
        if (bossData == null) return 0;
        return (float) bossCurrentHealth / bossData.maxHealth;
    }
    
    public float getPlayerHealthPercent() {
        return (float) playerCurrentHealth / playerMaxHealth;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void pause() {
        handler.removeCallbacksAndMessages(null);
    }
    
    public void resume() {
        // Resume based on current state
        if (currentState == BattleState.PLAYER_TURN) {
            startPlayerTurn();
        }
    }
    
    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        listener = null;
    }
}
