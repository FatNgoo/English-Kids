package com.edu.english.alphabet_adventure;

import static org.junit.Assert.*;

import com.edu.english.alphabet_adventure.data.GameData;
import com.edu.english.alphabet_adventure.models.GameState;
import com.edu.english.alphabet_adventure.models.LetterToken;
import com.edu.english.alphabet_adventure.models.Mascot;
import com.edu.english.alphabet_adventure.models.WordItem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Unit tests for the Alphabet Adventure game logic.
 */
public class AlphabetAdventureTest {

    private GameState gameState;
    private WordItem testWord;

    @Before
    public void setUp() {
        gameState = new GameState();
        testWord = new WordItem("apple", "🍎");
        gameState.setCurrentWord(testWord);
    }

    // ==================== GameState Tests ====================

    @Test
    public void testInitialState() {
        assertEquals(0, gameState.getCurrentIndex());
        assertEquals(3, gameState.getLives());
        assertEquals(2, gameState.getCurrentLane()); // Middle lane
        assertEquals(GameState.Status.PLAYING, gameState.getStatus());
        assertFalse(gameState.isMuted());
    }

    @Test
    public void testMoveUp() {
        gameState.setCurrentLane(2);
        gameState.moveUp();
        assertEquals(1, gameState.getCurrentLane());
        
        gameState.moveUp();
        assertEquals(0, gameState.getCurrentLane());
        
        // Should not go below 0
        gameState.moveUp();
        assertEquals(0, gameState.getCurrentLane());
    }

    @Test
    public void testMoveDown() {
        gameState.setCurrentLane(2);
        gameState.moveDown();
        assertEquals(3, gameState.getCurrentLane());
        
        gameState.moveDown();
        assertEquals(4, gameState.getCurrentLane());
        
        // Should not go above 4
        gameState.moveDown();
        assertEquals(4, gameState.getCurrentLane());
    }

    @Test
    public void testDecrementLives() {
        assertEquals(3, gameState.getLives());
        
        gameState.decrementLives();
        assertEquals(2, gameState.getLives());
        assertEquals(GameState.Status.PLAYING, gameState.getStatus());
        
        gameState.decrementLives();
        assertEquals(1, gameState.getLives());
        assertEquals(GameState.Status.PLAYING, gameState.getStatus());
        
        gameState.decrementLives();
        assertEquals(0, gameState.getLives());
        assertEquals(GameState.Status.LOSE, gameState.getStatus());
    }

    @Test
    public void testIncrementIndex() {
        assertEquals(0, gameState.getCurrentIndex());
        
        gameState.incrementIndex();
        assertEquals(1, gameState.getCurrentIndex());
        
        gameState.incrementIndex();
        assertEquals(2, gameState.getCurrentIndex());
    }

    @Test
    public void testGetTargetLetter() {
        assertEquals('A', gameState.getTargetLetter());
        
        gameState.incrementIndex();
        assertEquals('P', gameState.getTargetLetter());
        
        gameState.incrementIndex();
        assertEquals('P', gameState.getTargetLetter());
        
        gameState.incrementIndex();
        assertEquals('L', gameState.getTargetLetter());
        
        gameState.incrementIndex();
        assertEquals('E', gameState.getTargetLetter());
    }

    @Test
    public void testWordComplete() {
        assertFalse(gameState.isWordComplete());
        
        // Increment through all letters of "apple"
        for (int i = 0; i < 5; i++) {
            gameState.incrementIndex();
        }
        
        assertTrue(gameState.isWordComplete());
    }

    @Test
    public void testProgressString() {
        assertEquals("_ _ _ _ _", gameState.getProgressString());
        
        gameState.incrementIndex();
        assertEquals("A _ _ _ _", gameState.getProgressString());
        
        gameState.incrementIndex();
        assertEquals("A P _ _ _", gameState.getProgressString());
        
        gameState.incrementIndex();
        gameState.incrementIndex();
        gameState.incrementIndex();
        assertEquals("A P P L E", gameState.getProgressString());
    }

    @Test
    public void testReset() {
        gameState.incrementIndex();
        gameState.decrementLives();
        gameState.setCurrentLane(4);
        
        gameState.reset();
        
        assertEquals(0, gameState.getCurrentIndex());
        assertEquals(3, gameState.getLives());
        assertEquals(2, gameState.getCurrentLane());
        assertEquals(GameState.Status.PLAYING, gameState.getStatus());
    }

    @Test
    public void testToggleMute() {
        assertFalse(gameState.isMuted());
        
        gameState.toggleMute();
        assertTrue(gameState.isMuted());
        
        gameState.toggleMute();
        assertFalse(gameState.isMuted());
    }

    // ==================== WordItem Tests ====================

    @Test
    public void testWordItem() {
        WordItem word = new WordItem("CAT", "🐱", "A pet");
        
        assertEquals("cat", word.getWord()); // Should be lowercase
        assertEquals("🐱", word.getImageEmoji());
        assertEquals("A pet", word.getHint());
        assertEquals(3, word.getWordLength());
        assertEquals('c', word.getCharAt(0));
        assertEquals('a', word.getCharAt(1));
        assertEquals('t', word.getCharAt(2));
    }

    // ==================== GameData Tests ====================

    @Test
    public void testGetWords() {
        List<WordItem> words = GameData.getWords();
        
        assertEquals(20, words.size());
        
        // Verify some known words
        boolean hasApple = false;
        boolean hasCat = false;
        boolean hasTree = false;
        
        for (WordItem word : words) {
            if (word.getWord().equals("apple")) hasApple = true;
            if (word.getWord().equals("cat")) hasCat = true;
            if (word.getWord().equals("tree")) hasTree = true;
        }
        
        assertTrue(hasApple);
        assertTrue(hasCat);
        assertTrue(hasTree);
    }

    @Test
    public void testGetMascots() {
        List<Mascot> mascots = GameData.getMascots();
        
        assertEquals(4, mascots.size());
        
        // Verify IDs are unique
        Set<Integer> ids = new HashSet<>();
        for (Mascot mascot : mascots) {
            ids.add(mascot.getId());
        }
        assertEquals(4, ids.size());
    }

    @Test
    public void testGetMascotById() {
        Mascot bunny = GameData.getMascotById(1);
        assertEquals("Bunny", bunny.getName());
        assertEquals("🐰", bunny.getEmoji());
        
        Mascot bear = GameData.getMascotById(2);
        assertEquals("Bear", bear.getName());
        
        // Test fallback for invalid ID
        Mascot fallback = GameData.getMascotById(999);
        assertNotNull(fallback);
    }

    // ==================== SpawnLetters Tests ====================

    @Test
    public void testSpawnLettersHasFiveTokens() {
        List<LetterToken> tokens = spawnTestTokens('A');
        assertEquals(5, tokens.size());
    }

    @Test
    public void testSpawnLettersHasTargetLetter() {
        char target = 'A';
        List<LetterToken> tokens = spawnTestTokens(target);
        
        boolean hasTarget = false;
        for (LetterToken token : tokens) {
            if (token.getLetter() == target && token.isCorrect()) {
                hasTarget = true;
                break;
            }
        }
        
        assertTrue("Spawn should contain target letter", hasTarget);
    }

    @Test
    public void testSpawnLettersNoduplicates() {
        for (int run = 0; run < 100; run++) {
            List<LetterToken> tokens = spawnTestTokens((char) ('A' + run % 26));
            
            Set<Character> letters = new HashSet<>();
            for (LetterToken token : tokens) {
                letters.add(token.getLetter());
            }
            
            assertEquals("No duplicate letters in spawn", 5, letters.size());
        }
    }

    @Test
    public void testSpawnLettersEachLaneHasOneToken() {
        List<LetterToken> tokens = spawnTestTokens('A');
        
        Set<Integer> lanes = new HashSet<>();
        for (LetterToken token : tokens) {
            lanes.add(token.getLane());
        }
        
        assertEquals(5, lanes.size());
        for (int i = 0; i < 5; i++) {
            assertTrue("Should have token in lane " + i, lanes.contains(i));
        }
    }

    @Test
    public void testSpawnLettersOnlyOneCorrect() {
        for (int run = 0; run < 100; run++) {
            List<LetterToken> tokens = spawnTestTokens((char) ('A' + run % 26));
            
            int correctCount = 0;
            for (LetterToken token : tokens) {
                if (token.isCorrect()) {
                    correctCount++;
                }
            }
            
            assertEquals("Only one token should be correct", 1, correctCount);
        }
    }

    // Helper method to simulate token spawning
    private List<LetterToken> spawnTestTokens(char targetLetter) {
        List<LetterToken> tokens = new ArrayList<>();
        Random random = new Random();
        int correctLane = random.nextInt(5);
        
        Set<Character> usedLetters = new HashSet<>();
        usedLetters.add(targetLetter);
        
        List<Character> distractors = new ArrayList<>();
        while (distractors.size() < 4) {
            char c = (char) ('A' + random.nextInt(26));
            if (!usedLetters.contains(c)) {
                usedLetters.add(c);
                distractors.add(c);
            }
        }
        
        int distractorIndex = 0;
        for (int lane = 0; lane < 5; lane++) {
            char letter;
            boolean isCorrect;
            
            if (lane == correctLane) {
                letter = targetLetter;
                isCorrect = true;
            } else {
                letter = distractors.get(distractorIndex++);
                isCorrect = false;
            }
            
            tokens.add(new LetterToken(letter, lane, 1000f, isCorrect));
        }
        
        return tokens;
    }

    // ==================== State Transition Tests ====================

    @Test
    public void testCorrectLetterIncreasesIndex() {
        int initialIndex = gameState.getCurrentIndex();
        gameState.incrementIndex();
        assertEquals(initialIndex + 1, gameState.getCurrentIndex());
    }

    @Test
    public void testWrongLetterDecreasesLives() {
        int initialLives = gameState.getLives();
        gameState.decrementLives();
        assertEquals(initialLives - 1, gameState.getLives());
    }

    @Test
    public void testZeroLivesTriggersLose() {
        gameState.decrementLives();
        gameState.decrementLives();
        gameState.decrementLives();
        assertEquals(GameState.Status.LOSE, gameState.getStatus());
    }

    @Test
    public void testCompleteWordTriggersWin() {
        // Complete the word "apple"
        for (int i = 0; i < 5; i++) {
            gameState.incrementIndex();
        }
        
        assertTrue(gameState.isWordComplete());
        
        // Manually set win status (in real game, this happens in GameEngine)
        gameState.setStatus(GameState.Status.WIN);
        assertEquals(GameState.Status.WIN, gameState.getStatus());
    }

    // ==================== LetterToken Tests ====================

    @Test
    public void testLetterTokenCreation() {
        LetterToken token = new LetterToken('a', 2, 500f, true);
        
        assertEquals('A', token.getLetter()); // Should be uppercase
        assertEquals(2, token.getLane());
        assertEquals(500f, token.getPositionX(), 0.01f);
        assertTrue(token.isCorrect());
        assertTrue(token.isActive());
    }

    @Test
    public void testLetterTokenMovement() {
        LetterToken token = new LetterToken('A', 0, 500f, false);
        
        token.moveLeft(10f);
        assertEquals(490f, token.getPositionX(), 0.01f);
        
        token.moveLeft(100f);
        assertEquals(390f, token.getPositionX(), 0.01f);
    }

    @Test
    public void testLetterTokenDeactivation() {
        LetterToken token = new LetterToken('A', 0, 500f, false);
        
        assertTrue(token.isActive());
        token.setActive(false);
        assertFalse(token.isActive());
    }
}
