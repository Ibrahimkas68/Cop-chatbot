package com.assistant.smartsearch.domain.port;

/**
 * Service for detecting the language of a given text.
 * Supports Arabic and French language detection.
 */
public interface LanguageDetectionService {
    
    /**
     * Detects the language of the given text.
     * 
     * @param text the text to analyze
     * @return "arabic" or "french" based on the detected language
     */
    String detectLanguage(String text);
    
    /**
     * Checks if the given text contains Arabic characters.
     * 
     * @param text the text to check
     * @return true if the text contains Arabic characters, false otherwise
     */
    boolean containsArabic(String text);
    
    /**
     * Checks if the given text contains French characters.
     * 
     * @param text the text to check
     * @return true if the text contains French characters, false otherwise
     */
    boolean containsFrench(String text);
}