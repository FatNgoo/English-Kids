package com.edu.english.storybook.api;

import com.edu.english.storybook.model.StorySetup;

/**
 * Prompt builder for Gemini AI story generation
 */
public class StoryPromptBuilder {
    
    private static final String SYSTEM_CONTEXT = 
        "You are a children's story writer creating educational content for kids aged 5-10. " +
        "Your stories must be: warm, friendly, encouraging, educational, and always have happy/hopeful endings. " +
        "NEVER include: violence, scary content, blood, self-harm, sensitive topics, discrimination, or hate. " +
        "Use simple English appropriate for the reading level specified. " +
        "Always respond with ONLY valid JSON - no markdown, no extra text.";

    /**
     * Build prompt for Step 1: Generate story outline
     */
    public static String buildOutlinePrompt(StorySetup setup) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_CONTEXT).append("\n\n");
        
        prompt.append("Create a story outline with these requirements:\n");
        prompt.append("- Category: ").append(setup.getCategory()).append("\n");
        prompt.append("- Target age: ").append(setup.getAge()).append(" years old\n");
        prompt.append("- Reading level: ").append(setup.getReadingLevel()).append(" (");
        prompt.append(setup.getReadingLevel().equals("A1") ? "very simple" : "simple").append(" English)\n");
        prompt.append("- Number of chapters: ").append(setup.getChapterCount()).append("\n");
        
        if (setup.getTheme() != null && !setup.getTheme().isEmpty()) {
            prompt.append("- Theme/topic: ").append(setup.getTheme()).append("\n");
        }
        
        if (setup.getCharacterName() != null && !setup.getCharacterName().isEmpty()) {
            prompt.append("- Main character name: ").append(setup.getCharacterName()).append("\n");
        }
        
        prompt.append("\nRespond with ONLY this JSON structure:\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"Story title here\",\n");
        prompt.append("  \"chapters\": [\n");
        prompt.append("    {\"heading\": \"Chapter 1 title\", \"summary\": \"1-2 sentence summary\"},\n");
        prompt.append("    ...\n");
        prompt.append("  ],\n");
        prompt.append("  \"vocabSeed\": [\"word1\", \"word2\", ... (20 words for vocabulary)],\n");
        prompt.append("  \"questionTopics\": [\"topic1\", \"topic2\", ... (5 topics for quiz)]\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for Step 2: Generate a single chapter
     */
    public static String buildChapterPrompt(StorySetup setup, String storyTitle, 
                                            String chapterHeading, String chapterSummary,
                                            int chapterNumber, int totalChapters) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_CONTEXT).append("\n\n");
        
        prompt.append("Write chapter ").append(chapterNumber).append(" of ").append(totalChapters);
        prompt.append(" for the story \"").append(storyTitle).append("\".\n\n");
        
        prompt.append("Story context:\n");
        prompt.append("- Category: ").append(setup.getCategory()).append("\n");
        prompt.append("- Target age: ").append(setup.getAge()).append(" years old\n");
        prompt.append("- Reading level: ").append(setup.getReadingLevel()).append("\n");
        
        if (setup.getCharacterName() != null && !setup.getCharacterName().isEmpty()) {
            prompt.append("- Main character: ").append(setup.getCharacterName()).append("\n");
        }
        
        prompt.append("\nChapter details:\n");
        prompt.append("- Heading: ").append(chapterHeading).append("\n");
        prompt.append("- Summary: ").append(chapterSummary).append("\n");
        
        // Target word count per chapter based on total
        int[] wordRange = setup.getWordCountRange();
        int wordsPerChapter = (wordRange[0] + wordRange[1]) / 2 / setup.getChapterCount();
        prompt.append("- Target length: approximately ").append(wordsPerChapter).append(" words\n");
        
        prompt.append("\nWrite engaging, age-appropriate content. Use short sentences and simple vocabulary.\n");
        prompt.append("\nRespond with ONLY this JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"heading\": \"").append(chapterHeading).append("\",\n");
        prompt.append("  \"content\": \"Full chapter text here...\"\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for Step 3: Generate vocabulary and quiz
     */
    public static String buildVocabQuizPrompt(StorySetup setup, String storyTitle, 
                                              java.util.List<String> vocabSeed,
                                              java.util.List<String> questionTopics) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_CONTEXT).append("\n\n");
        
        prompt.append("Create vocabulary list and quiz for the story \"").append(storyTitle).append("\".\n\n");
        
        prompt.append("Context:\n");
        prompt.append("- Target age: ").append(setup.getAge()).append(" years old\n");
        prompt.append("- Reading level: ").append(setup.getReadingLevel()).append("\n\n");
        
        prompt.append("Vocabulary words to include (pick 10-15): ");
        if (vocabSeed != null && !vocabSeed.isEmpty()) {
            prompt.append(String.join(", ", vocabSeed));
        }
        prompt.append("\n\n");
        
        prompt.append("Quiz topics: ");
        if (questionTopics != null && !questionTopics.isEmpty()) {
            prompt.append(String.join(", ", questionTopics));
        }
        prompt.append("\n\n");
        
        prompt.append("Respond with ONLY this JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"vocabulary\": [\n");
        prompt.append("    {\"word\": \"English word\", \"meaning_vi\": \"Vietnamese meaning\", \"exampleSentence\": \"Simple example\"},\n");
        prompt.append("    ...\n");
        prompt.append("  ],\n");
        prompt.append("  \"questions\": [\n");
        prompt.append("    {\"q\": \"Question text?\", \"choices\": [\"A\", \"B\", \"C\", \"D\"], \"answerIndex\": 0},\n");
        prompt.append("    ... (5 questions total)\n");
        prompt.append("  ]\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * Build retry prompt when JSON parsing fails
     */
    public static String buildRetryPrompt(String originalPrompt) {
        return originalPrompt + "\n\nIMPORTANT: Your previous response was not valid JSON. " +
               "Return ONLY valid JSON with no additional text or markdown.";
    }
}
