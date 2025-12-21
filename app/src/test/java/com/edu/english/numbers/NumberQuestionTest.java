package com.edu.english.numbers;

import com.edu.english.numbers.models.NumberQuestion;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for NumberQuestion class
 * Tests answer checking logic and speech normalization
 */
public class NumberQuestionTest {

    @Test
    public void testCorrectNumberAnswer() {
        NumberQuestion question = new NumberQuestion(1, "How many apples?", 5, "five");
        
        assertTrue(question.isCorrectNumber(5));
        assertFalse(question.isCorrectNumber(4));
        assertFalse(question.isCorrectNumber(6));
        assertFalse(question.isCorrectNumber(0));
    }

    @Test
    public void testCorrectSpeechExactMatch() {
        NumberQuestion question = new NumberQuestion(1, "Test", 5, "five");
        
        assertTrue(question.isCorrectSpeech("five"));
        assertTrue(question.isCorrectSpeech("Five"));
        assertTrue(question.isCorrectSpeech("FIVE"));
        assertTrue(question.isCorrectSpeech("  five  "));
    }

    @Test
    public void testCorrectSpeechWithPunctuation() {
        NumberQuestion question = new NumberQuestion(1, "Test", 3, "three");
        
        assertTrue(question.isCorrectSpeech("three."));
        assertTrue(question.isCorrectSpeech("three!"));
        assertTrue(question.isCorrectSpeech("three?"));
        assertTrue(question.isCorrectSpeech("\"three\""));
    }

    @Test
    public void testSpeechVariantsOne() {
        NumberQuestion question = new NumberQuestion(1, "Test", 1, "one");
        
        assertTrue(question.isCorrectSpeech("one"));
        assertTrue(question.isCorrectSpeech("won")); // Common misrecognition
        assertTrue(question.isCorrectSpeech("1"));
    }

    @Test
    public void testSpeechVariantsTwo() {
        NumberQuestion question = new NumberQuestion(1, "Test", 2, "two");
        
        assertTrue(question.isCorrectSpeech("two"));
        assertTrue(question.isCorrectSpeech("to"));   // Common homophone
        assertTrue(question.isCorrectSpeech("too"));  // Common homophone
        assertTrue(question.isCorrectSpeech("2"));
    }

    @Test
    public void testSpeechVariantsThree() {
        NumberQuestion question = new NumberQuestion(1, "Test", 3, "three");
        
        assertTrue(question.isCorrectSpeech("three"));
        assertTrue(question.isCorrectSpeech("tree")); // Common misrecognition
        assertTrue(question.isCorrectSpeech("3"));
    }

    @Test
    public void testSpeechVariantsFour() {
        NumberQuestion question = new NumberQuestion(1, "Test", 4, "four");
        
        assertTrue(question.isCorrectSpeech("four"));
        assertTrue(question.isCorrectSpeech("for"));  // Common homophone
        assertTrue(question.isCorrectSpeech("fore")); // Common homophone
        assertTrue(question.isCorrectSpeech("4"));
    }

    @Test
    public void testSpeechVariantsEight() {
        NumberQuestion question = new NumberQuestion(1, "Test", 8, "eight");
        
        assertTrue(question.isCorrectSpeech("eight"));
        assertTrue(question.isCorrectSpeech("ate")); // Common homophone
        assertTrue(question.isCorrectSpeech("8"));
    }

    @Test
    public void testWrongSpeech() {
        NumberQuestion question = new NumberQuestion(1, "Test", 5, "five");
        
        assertFalse(question.isCorrectSpeech("four"));
        assertFalse(question.isCorrectSpeech("six"));
        assertFalse(question.isCorrectSpeech("apple"));
        assertFalse(question.isCorrectSpeech(""));
        assertFalse(question.isCorrectSpeech(null));
    }

    @Test
    public void testFocusKeyword() {
        NumberQuestion withFocus = new NumberQuestion(1, "Test", 5, "five", "red apples");
        NumberQuestion withoutFocus = new NumberQuestion(1, "Test", 5, "five");
        
        assertTrue(withFocus.hasFocusKeyword());
        assertEquals("red apples", withFocus.getFocusKeyword());
        
        assertFalse(withoutFocus.hasFocusKeyword());
        assertNull(withoutFocus.getFocusKeyword());
    }

    @Test
    public void testGetters() {
        NumberQuestion question = new NumberQuestion(2, "How many cars?", 6, "six", "cars");
        
        assertEquals(2, question.getSceneId());
        assertEquals("How many cars?", question.getPrompt());
        assertEquals(6, question.getAnswerNumber());
        assertEquals("six", question.getAnswerWord());
        assertEquals("cars", question.getFocusKeyword());
    }
}
