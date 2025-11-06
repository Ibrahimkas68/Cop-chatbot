package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.port.LanguageDetectionService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementation of language detection service.
 * Uses character pattern analysis to detect Arabic vs French text.
 */
@Service
public class LanguageDetectionAdapter implements LanguageDetectionService {
    
    // Arabic Unicode ranges
    private static final Pattern ARABIC_PATTERN = Pattern.compile(
        "[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF]"
    );
    
    // French/Latin characters (including accented characters)
    private static final Pattern FRENCH_PATTERN = Pattern.compile(
        "[a-zA-ZÀ-ÿ\\u00C0-\\u017F\\u0100-\\u017F\\u0180-\\u024F]"
    );
    
    @Override
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "french"; // Default to French
        }
        
        String cleanText = text.trim();
        
        // Count Arabic and French characters
        int arabicCount = countMatches(cleanText, ARABIC_PATTERN);
        int frenchCount = countMatches(cleanText, FRENCH_PATTERN);
        
        // If no characters detected, default to French
        if (arabicCount == 0 && frenchCount == 0) {
            return "french";
        }
        
        // Return the language with more characters
        return arabicCount >= frenchCount ? "arabic" : "french";
    }
    
    @Override
    public boolean containsArabic(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return ARABIC_PATTERN.matcher(text).find();
    }
    
    @Override
    public boolean containsFrench(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return FRENCH_PATTERN.matcher(text).find();
    }
    
    /**
     * Counts the number of matches for a given pattern in the text.
     */
    private int countMatches(String text, Pattern pattern) {
        int count = 0;
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}