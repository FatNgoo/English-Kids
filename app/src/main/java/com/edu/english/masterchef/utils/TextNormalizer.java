package com.edu.english.masterchef.utils;

import java.util.Locale;

/**
 * Utility class for normalizing speech recognition text for comparison.
 */
public class TextNormalizer {
    
    /**
     * Normalize text for speech comparison:
     * - Lowercase
     * - Remove punctuation
     * - Trim whitespace
     * - Handle common variations
     */
    public static String normalize(String text) {
        if (text == null) return "";
        
        String result = text.toLowerCase(Locale.US);
        
        // Remove punctuation
        result = result.replaceAll("[^a-z0-9\\s]", "");
        
        // Normalize whitespace
        result = result.replaceAll("\\s+", " ").trim();
        
        return result;
    }
    
    /**
     * Check if spoken text matches expected phrase.
     * Allows for minor variations.
     */
    public static boolean matches(String spoken, String expected) {
        String normalizedSpoken = normalize(spoken);
        String normalizedExpected = normalize(expected);
        
        // Exact match
        if (normalizedSpoken.equals(normalizedExpected)) {
            return true;
        }
        
        // Check if spoken contains expected
        if (normalizedSpoken.contains(normalizedExpected)) {
            return true;
        }
        
        // Check without articles (the, a, an)
        String spokenNoArticles = removeArticles(normalizedSpoken);
        String expectedNoArticles = removeArticles(normalizedExpected);
        
        if (spokenNoArticles.equals(expectedNoArticles)) {
            return true;
        }
        
        // Fuzzy match - allow up to 20% difference
        return calculateSimilarity(normalizedSpoken, normalizedExpected) >= 0.8;
    }
    
    /**
     * Remove common articles from text.
     */
    private static String removeArticles(String text) {
        return text
            .replaceAll("\\bthe\\b", "")
            .replaceAll("\\ba\\b", "")
            .replaceAll("\\ban\\b", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
    
    /**
     * Calculate similarity between two strings using Levenshtein distance.
     * Returns value between 0 (no match) and 1 (exact match).
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.equals(s2)) return 1;
        if (s1.isEmpty() || s2.isEmpty()) return 0;
        
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[m][n];
    }
    
    /**
     * Get key words from a phrase for partial matching.
     */
    public static String[] getKeyWords(String phrase) {
        String normalized = normalize(phrase);
        String noArticles = removeArticles(normalized);
        return noArticles.split("\\s+");
    }
    
    /**
     * Check if spoken text contains all key words from expected phrase.
     */
    public static boolean containsAllKeyWords(String spoken, String expected) {
        String[] keyWords = getKeyWords(expected);
        String normalizedSpoken = normalize(spoken);
        
        for (String word : keyWords) {
            if (!word.isEmpty() && !normalizedSpoken.contains(word)) {
                return false;
            }
        }
        return true;
    }
}
