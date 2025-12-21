package com.edu.english.numberdash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages questions for Number Dash Race game
 */
public class QuestionManager {
    
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Question currentQuestion;
    private Random random;
    
    public QuestionManager() {
        random = new Random();
        questions = new ArrayList<>();
        generateQuestions();
    }
    
    /**
     * Generate a set of questions for the race
     */
    public void generateQuestions() {
        questions.clear();
        currentQuestionIndex = 0;
        
        // Mix of different question types
        // Type 1: Simple counting (1-5) - 2 questions
        for (int i = 0; i < 2; i++) {
            questions.add(generateCountingQuestion(1, 5));
        }
        
        // Type 2: Medium counting (5-10) - 2 questions
        for (int i = 0; i < 2; i++) {
            questions.add(generateCountingQuestion(5, 10));
        }
        
        // Type 3: Simple addition - 2 questions
        for (int i = 0; i < 2; i++) {
            questions.add(generateAdditionQuestion());
        }
        
        // Shuffle questions
        Collections.shuffle(questions);
    }
    
    /**
     * Generate a counting question
     */
    private Question generateCountingQuestion(int minCount, int maxCount) {
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        String[] objects = {"🍎", "🌟", "🎈", "🍌", "🍊", "🐱", "🐶", "🦋", "🌈", "🍕"};
        String[] objectNames = {"apples", "stars", "balloons", "bananas", "oranges", 
                               "cats", "dogs", "butterflies", "rainbows", "pizzas"};
        
        int objIndex = random.nextInt(objects.length);
        String object = objects[objIndex];
        String objectName = objectNames[objIndex];
        
        // Build visual representation
        StringBuilder visualBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            visualBuilder.append(object);
            if (i < count - 1) {
                visualBuilder.append(" ");
            }
        }
        
        String questionText = "How many " + objectName + "?";
        
        // Generate answer options
        int[] options = generateOptions(count, 1, 10);
        
        return new Question(questionText, visualBuilder.toString(), count, options, Question.QuestionType.COUNTING);
    }
    
    /**
     * Generate a simple addition question
     */
    private Question generateAdditionQuestion() {
        int num1 = random.nextInt(4) + 1; // 1-4
        int num2 = random.nextInt(4) + 1; // 1-4
        int answer = num1 + num2;
        
        String[] objects1 = {"🍎", "🌟", "🎈"};
        String[] objects2 = {"🍌", "🍊", "🎁"};
        
        int idx1 = random.nextInt(objects1.length);
        int idx2 = random.nextInt(objects2.length);
        
        StringBuilder visual1 = new StringBuilder();
        for (int i = 0; i < num1; i++) {
            visual1.append(objects1[idx1]);
        }
        
        StringBuilder visual2 = new StringBuilder();
        for (int i = 0; i < num2; i++) {
            visual2.append(objects2[idx2]);
        }
        
        String visualRepresentation = visual1.toString() + " + " + visual2.toString();
        String questionText = "Count them all!";
        
        int[] options = generateOptions(answer, 2, 10);
        
        return new Question(questionText, visualRepresentation, answer, options, Question.QuestionType.ADDITION);
    }
    
    /**
     * Generate answer options including the correct answer
     */
    private int[] generateOptions(int correctAnswer, int minValue, int maxValue) {
        int[] options = new int[3];
        options[0] = correctAnswer;
        
        // Generate two wrong answers
        int wrongAnswer1, wrongAnswer2;
        do {
            wrongAnswer1 = random.nextInt(maxValue - minValue + 1) + minValue;
        } while (wrongAnswer1 == correctAnswer);
        
        do {
            wrongAnswer2 = random.nextInt(maxValue - minValue + 1) + minValue;
        } while (wrongAnswer2 == correctAnswer || wrongAnswer2 == wrongAnswer1);
        
        options[1] = wrongAnswer1;
        options[2] = wrongAnswer2;
        
        // Shuffle options
        for (int i = options.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = options[i];
            options[i] = options[j];
            options[j] = temp;
        }
        
        return options;
    }
    
    /**
     * Get the next question
     */
    public Question getNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            currentQuestion = questions.get(currentQuestionIndex);
            currentQuestionIndex++;
            return currentQuestion;
        }
        return null;
    }
    
    /**
     * Check if the answer is correct
     */
    public boolean checkAnswer(int answer) {
        if (currentQuestion != null) {
            return currentQuestion.getCorrectAnswer() == answer;
        }
        return false;
    }
    
    /**
     * Check if there are more questions
     */
    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size();
    }
    
    /**
     * Get current question
     */
    public Question getCurrentQuestion() {
        return currentQuestion;
    }
    
    /**
     * Reset for a new game
     */
    public void reset() {
        generateQuestions();
    }
    
    /**
     * Get total number of questions
     */
    public int getTotalQuestions() {
        return questions.size();
    }
    
    /**
     * Get number of questions answered
     */
    public int getQuestionsAnswered() {
        return currentQuestionIndex;
    }
}
