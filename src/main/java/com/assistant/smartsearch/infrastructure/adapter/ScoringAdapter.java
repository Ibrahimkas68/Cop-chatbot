package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.model.ScoreDetails;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.port.ScoringEngine;
import com.assistant.smartsearch.domain.port.LanguageDetectionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScoringAdapter implements ScoringEngine {

    private final LanguageDetectionService languageDetectionService;

    public ScoringAdapter(LanguageDetectionService languageDetectionService) {
        this.languageDetectionService = languageDetectionService;
    }

    public List<SearchResult> scoreAndSort(
            List<Map<String, Object>> rawResults,
            List<String> keywords,
            List<String> searchFields,
            String query,
            String tableName) {

        List<SearchResult> results = new ArrayList<>();

        for (Map<String, Object> row : rawResults) {
            SearchResult result = new SearchResult();
            result.setOriginalId(((Number) row.get("id")).longValue());
            result.setData(row);
            result.setQuery(query);
            result.setTableName(tableName);

            // Detect language from query
            String detectedLanguage = languageDetectionService.detectLanguage(query);
            
            // Populate title, url, imageName, and description based on detected language
            String title = getTitle(row, detectedLanguage);
            result.setTitle(title);
            // Build URL possibly based on profile/advice_profile for specific tables
            result.setUrl(createUrl(tableName, (String) row.get("slug"), row));
            result.setImageName((String) row.get("image_name"));
            result.setDescription(getDescription(row, detectedLanguage));

            // Compute tag based on table and profile fields
            String tag = buildTag(tableName, row);
            result.setTag(tag);

            // Strict language-only response: skip entries without content in detected language
            if ("No Title".equals(result.getTitle()) && "No description available".equals(result.getDescription())) {
                continue;
            }


            ScoreDetails scoreDetails = calculateScore(row, keywords, searchFields, title);
            result.setScore(scoreDetails.getScore());
            result.setMatchedKeywords(scoreDetails.getMatchedKeywords());

            results.add(result);
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return results;
    }

    private String getTitle(Map<String, Object> row, String language) {
        String[] titleFields;
        
        // Strictly use fields based on detected language (no cross-language fallback)
        if ("arabic".equals(language)) {
            titleFields = new String[]{
                "tuile_title_ar", "title_ar", "name_ar", "word_ar"
            };
        } else {
            // Default to French
            titleFields = new String[]{
                "tuile_title_fr", "title_fr", "name_fr", "word_fr"
            };
        }
        
        for (String field : titleFields) {
            if (row.containsKey(field) && row.get(field) != null) {
                String value = (String) row.get(field);
                if (!value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return "No Title";
    }

    private String getDescription(Map<String, Object> row, String language) {
        String[] descriptionFields;
        
        // Strictly use fields based on detected language (no cross-language fallback)
        if ("arabic".equals(language)) {
            descriptionFields = new String[]{
                "mini_summary_ar", "summary_ar", "details_ar", "definition_ar",
                "tuile_text_ar", "description_ar", "content_ar"
            };
        } else {
            // Default to French
            descriptionFields = new String[]{
                "mini_summary_fr", "summary_fr", "details_fr", "definition_fr",
                "tuile_text_fr", "description_fr", "content_fr"
            };
        }
        
        for (String field : descriptionFields) {
            if (row.containsKey(field) && row.get(field) != null) {
                String value = (String) row.get(field);
                if (!value.trim().isEmpty()) {
                    // Clean HTML tags and limit length for description
                    String cleanValue = value.replaceAll("<[^>]*>", "").trim();
                    if (cleanValue.length() > 200) {
                        cleanValue = cleanValue.substring(0, 200) + "...";
                    }
                    return cleanValue;
                }
            }
        }
        return "No description available";
    }

    private String createUrl(String tableName, String slug) {
        // Backward-compatible default behavior for non-profile-based URLs
        if (slug == null || slug.isEmpty()) {
            if ("actualities".equals(tableName)) {
                return "https://www.e-himaya.gov.ma/actualites";
            }
        }

        if ("actualities".equals(tableName)) {
            return "https://www.e-himaya.gov.ma/actualites/" + slug;
        }

        if ("articles".equals(tableName)) {
            return "https://www.e-himaya.gov.ma/articles/slug/" + slug;
        }
        return "https://www.e-himaya.gov.ma/" + tableName + "/" + slug;
    }

    private String createUrl(String tableName, String slug, Map<String, Object> row) {
        // Profile-based URLs for guides and advices
        if ("guides".equalsIgnoreCase(tableName) || "advices".equalsIgnoreCase(tableName)) {
            String rawProfile = null;
            if ("guides".equalsIgnoreCase(tableName)) {
                // Try common column names for guides
                rawProfile = getString(row, "profile");
                if (rawProfile == null) rawProfile = getString(row, "profiles");
            } else {
                // advices
                rawProfile = getString(row, "advice_profile");
                if (rawProfile == null) rawProfile = getString(row, "profile");
            }

            String profile = normalizeProfile(rawProfile);
            String base = profileHome(profile);
            if (base != null) {
                return base; // As requested: return the home URL for the profile
            }
            // Fallback to non-profile URL rules if profile not recognized
            return createUrl(tableName, slug);
        }

        // Default behavior for other tables
        return createUrl(tableName, slug);
    }

    private String buildTag(String tableName, Map<String, Object> row) {
        if ("actualities".equalsIgnoreCase(tableName)) {
            return "actualites";
        }
        if ("guides".equalsIgnoreCase(tableName)) {
            String profile = normalizeProfile(getString(row, "profile"));
            if (profile == null) profile = normalizeProfile(getString(row, "profiles"));
            if (profile != null) {
                return "GUIDES." + profile;
            }
            return "GUIDES";
        }
        if ("advices".equalsIgnoreCase(tableName)) {
            String profile = normalizeProfile(getString(row, "advice_profile"));
            if (profile == null) profile = normalizeProfile(getString(row, "profile"));
            if (profile != null) {
                return "ADVICES." + profile;
            }
            return "ADVICES";
        }
        return tableName;
    }

    private String getString(Map<String, Object> row, String key) {
        if (row == null || key == null) return null;
        Object val = row.get(key);
        if (val == null) return null;
        String s = String.valueOf(val).trim();
        return s.isEmpty() ? null : s;
    }

    private String normalizeProfile(String raw) {
        if (raw == null) return null;
        String up = raw.trim().toUpperCase();
        // Handle possible variants
        if (up.startsWith("PARENT")) return "PARENTS";
        if (up.startsWith("TEACHER") || up.contains("ENSEIGNANT")) return "TEACHERS";
        if (up.startsWith("YOUTH") || up.contains("JEUNE")) return "YOUTH";
        return up; // unknown but keep uppercase
    }

    private String profileHome(String profile) {
        if (profile == null) return null;
        switch (profile) {
            case "PARENTS":
                return "https://www.e-himaya.gov.ma/parents/home";
            case "TEACHERS":
                return "https://www.e-himaya.gov.ma/enseignants/home";
            case "YOUTH":
                return "https://www.e-himaya.gov.ma/jeunes/home";
            default:
                return null;
        }
    }


    private ScoreDetails calculateScore(
            Map<String, Object> row,
            List<String> keywords,
            List<String> searchFields,
            String title) {

        double totalScore = 0.0;
        List<String> matched = new ArrayList<>();

        // Title scoring
        if (title != null && !title.isEmpty()) {
            String lowerCaseTitle = title.toLowerCase();
            for (String keyword : keywords) {
                if (lowerCaseTitle.contains(keyword)) {
                    totalScore += 20; // Higher score for title match
                    if (!matched.contains(keyword)) {
                        matched.add(keyword);
                    }
                }
            }
        }

        for (String field : searchFields) {
            if (row.get(field) == null) {
                continue;
            }
            String fieldValue = String.valueOf(row.get(field)).toLowerCase();

            for (String keyword : keywords) {
                int occurrences = countOccurrences(fieldValue, keyword);
                if (occurrences > 0) {
                    totalScore += occurrences * 10; // 10 points per occurrence
                    if (!matched.contains(keyword)) {
                        matched.add(keyword);
                    }
                }
            }
        }

        double maxPossibleScore = (keywords.size() * 20) + (keywords.size() * searchFields.size() * 10);
        if (maxPossibleScore == 0) {
            return new ScoreDetails(0.0, matched);
        }
        double percentage = (totalScore / maxPossibleScore) * 100;

        return new ScoreDetails(Math.min(percentage, 100.0), matched);
    }

    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }
}
