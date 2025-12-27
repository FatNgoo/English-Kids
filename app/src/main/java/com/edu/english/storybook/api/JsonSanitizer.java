package com.edu.english.storybook.api;

/**
 * Utility class to sanitize JSON responses from AI
 * Handles cases where model returns extra text around JSON
 */
public class JsonSanitizer {
    
    /**
     * Extract valid JSON from a response that may contain extra text
     */
    public static String sanitize(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        // Trim whitespace
        response = response.trim();
        
        // Remove markdown code blocks if present
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        response = response.trim();
        
        // Find the first { and last }
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');
        
        // If we found valid braces
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return response.substring(firstBrace, lastBrace + 1);
        }
        
        // Try array format
        int firstBracket = response.indexOf('[');
        int lastBracket = response.lastIndexOf(']');
        
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return response.substring(firstBracket, lastBracket + 1);
        }
        
        // Return original if no JSON found
        return response;
    }
    
    /**
     * Check if string is likely valid JSON
     */
    public static boolean isLikelyJson(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) ||
               (str.startsWith("[") && str.endsWith("]"));
    }
}
