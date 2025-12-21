package com.edu.english.numbers;

import com.edu.english.numbers.models.NumberQuestion;
import com.edu.english.numbers.utils.QuestionRandomizer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for QuestionRandomizer class
 * Tests shuffling and non-repetition logic
 */
public class QuestionRandomizerTest {

    private List<NumberQuestion> testQuestions;

    @Before
    public void setUp() {
        testQuestions = new ArrayList<>();
        testQuestions.add(new NumberQuestion(1, "Q1", 1, "one"));
        testQuestions.add(new NumberQuestion(1, "Q2", 2, "two"));
        testQuestions.add(new NumberQuestion(2, "Q3", 3, "three"));
        testQuestions.add(new NumberQuestion(2, "Q4", 4, "four"));
        testQuestions.add(new NumberQuestion(3, "Q5", 5, "five"));
        testQuestions.add(new NumberQuestion(3, "Q6", 6, "six"));
        testQuestions.add(new NumberQuestion(4, "Q7", 7, "seven"));
        testQuestions.add(new NumberQuestion(4, "Q8", 8, "eight"));
    }

    @Test
    public void testNoRepeatsBeforePoolExhausted() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        
        Set<String> seenQuestions = new HashSet<>();
        
        // Get all questions in first cycle
        for (int i = 0; i < testQuestions.size(); i++) {
            NumberQuestion q = randomizer.getNextQuestion();
            String key = q.getPrompt();
            
            // Should not see same question twice in one cycle
            assertFalse("Question repeated before pool exhausted: " + key, 
                    seenQuestions.contains(key));
            seenQuestions.add(key);
        }
        
        // All questions should have been seen
        assertEquals(testQuestions.size(), seenQuestions.size());
    }

    @Test
    public void testReshuffleAfterPoolExhausted() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        
        // Exhaust first cycle
        for (int i = 0; i < testQuestions.size(); i++) {
            randomizer.getNextQuestion();
        }
        
        // Second cycle should also have all questions
        Set<String> secondCycle = new HashSet<>();
        for (int i = 0; i < testQuestions.size(); i++) {
            NumberQuestion q = randomizer.getNextQuestion();
            String key = q.getPrompt();
            assertFalse("Question repeated in second cycle: " + key, 
                    secondCycle.contains(key));
            secondCycle.add(key);
        }
        
        assertEquals(testQuestions.size(), secondCycle.size());
    }

    @Test
    public void testHasMoreQuestions() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        
        // Initially has more
        assertTrue(randomizer.hasMoreQuestions());
        
        // After getting all, should be false (until reshuffle)
        for (int i = 0; i < testQuestions.size(); i++) {
            randomizer.getNextQuestion();
        }
        
        assertFalse(randomizer.hasMoreQuestions());
    }

    @Test
    public void testProgressTracking() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        
        assertEquals(0, randomizer.getCurrentQuestionNumber());
        assertEquals(testQuestions.size(), randomizer.getRemainingInCycle());
        
        randomizer.getNextQuestion();
        
        assertEquals(1, randomizer.getCurrentQuestionNumber());
        assertEquals(testQuestions.size() - 1, randomizer.getRemainingInCycle());
        
        randomizer.getNextQuestion();
        randomizer.getNextQuestion();
        
        assertEquals(3, randomizer.getCurrentQuestionNumber());
        assertEquals(testQuestions.size() - 3, randomizer.getRemainingInCycle());
    }

    @Test
    public void testGetTotalQuestions() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        assertEquals(testQuestions.size(), randomizer.getTotalQuestions());
    }

    @Test
    public void testResetAndShuffle() {
        QuestionRandomizer randomizer = new QuestionRandomizer(testQuestions);
        
        // Get some questions
        randomizer.getNextQuestion();
        randomizer.getNextQuestion();
        
        assertEquals(2, randomizer.getCurrentQuestionNumber());
        
        // Reset
        randomizer.resetAndShuffle();
        
        assertEquals(0, randomizer.getCurrentQuestionNumber());
        assertEquals(testQuestions.size(), randomizer.getRemainingInCycle());
    }

    @Test
    public void testShufflingOccurs() {
        // This test verifies that shuffling actually happens
        // Run multiple times and check that order varies
        
        List<String> firstOrder = new ArrayList<>();
        QuestionRandomizer randomizer1 = new QuestionRandomizer(testQuestions);
        for (int i = 0; i < testQuestions.size(); i++) {
            firstOrder.add(randomizer1.getNextQuestion().getPrompt());
        }
        
        boolean foundDifferentOrder = false;
        for (int attempt = 0; attempt < 10; attempt++) {
            List<String> anotherOrder = new ArrayList<>();
            QuestionRandomizer randomizer2 = new QuestionRandomizer(testQuestions);
            for (int i = 0; i < testQuestions.size(); i++) {
                anotherOrder.add(randomizer2.getNextQuestion().getPrompt());
            }
            
            if (!firstOrder.equals(anotherOrder)) {
                foundDifferentOrder = true;
                break;
            }
        }
        
        // It's statistically very unlikely to get same order 10 times
        assertTrue("Shuffling should produce different orders", foundDifferentOrder);
    }

    @Test
    public void testSingleQuestion() {
        List<NumberQuestion> singleQuestion = new ArrayList<>();
        singleQuestion.add(new NumberQuestion(1, "Only", 1, "one"));
        
        QuestionRandomizer randomizer = new QuestionRandomizer(singleQuestion);
        
        NumberQuestion q1 = randomizer.getNextQuestion();
        NumberQuestion q2 = randomizer.getNextQuestion();
        NumberQuestion q3 = randomizer.getNextQuestion();
        
        assertEquals("Only", q1.getPrompt());
        assertEquals("Only", q2.getPrompt());
        assertEquals("Only", q3.getPrompt());
    }
}
