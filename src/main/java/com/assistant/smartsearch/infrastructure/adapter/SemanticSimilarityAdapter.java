package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.port.SemanticSimilarityPort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class SemanticSimilarityAdapter implements SemanticSimilarityPort {
    
    // Predefined semantic groups - words that are related
    private static final Map<String, Set<String>> SEMANTIC_GROUPS = new HashMap<>();
    
    static {
        // Security/Defence group
        Set<String> securityGroup = new HashSet<>(Arrays.asList(
            "security", "securite", "sécurité", "seguridad",
            "defence", "defense", "défense", "defensa",
            "protection", "protect", "protéger", "proteger",
            "cybersecurity", "cybersécurité", "cyber",
            "safety", "sûreté", "safe", "sécurisé",
            "guard", "guardian", "gardien",
            "shield", "bouclier",
            "firewall", "pare-feu",
            "encryption", "chiffrement", "cryptage",
            "attack", "attaque", "threat", "menace",
            "vulnerability", "vulnérabilité", "faille",
            "breach", "intrusion", "piratage", "hack"
        ));
        SEMANTIC_GROUPS.put("security", securityGroup);
        
        // Network group
        Set<String> networkGroup = new HashSet<>(Arrays.asList(
            "network", "réseau", "red",
            "internet", "web", "online", "en ligne",
            "connection", "connexion", "conectar",
            "wifi", "wireless", "sans fil"
        ));
        SEMANTIC_GROUPS.put("network", networkGroup);
        
        // Data/Privacy group
        Set<String> dataGroup = new HashSet<>(Arrays.asList(
            "data", "données", "datos",
            "privacy", "vie privée", "privacidad",
            "confidential", "confidentiel",
            "personal", "personnel", "private", "privé",
            "gdpr", "rgpd"
        ));
        SEMANTIC_GROUPS.put("data", dataGroup);
        
        // Learning/Education group
        Set<String> learningGroup = new HashSet<>(Arrays.asList(
            "learn", "apprendre", "aprender", "learning",
            "education", "éducation", "educación",
            "training", "formation", "formación",
            "teach", "enseigner", "enseñar",
            "study", "étudier", "estudiar",
            "course", "cours", "curso",
            "guide", "tutorial", "tutoriel"
        ));
        SEMANTIC_GROUPS.put("learning", learningGroup);
    }
    
    /**
     * Get all related terms for a given word
     */
    public Set<String> getRelatedTerms(String term) {
        String normalizedTerm = normalize(term);
        Set<String> relatedTerms = new HashSet<>();
        relatedTerms.add(normalizedTerm); // Always include the original term
        
        // Find which semantic group this term belongs to
        for (Set<String> group : SEMANTIC_GROUPS.values()) {
            if (containsNormalized(group, normalizedTerm)) {
                // Add all terms from this group
                relatedTerms.addAll(group);
                break;
            }
        }
        
        return relatedTerms;
    }
    
    /**
     * Expand a search query with related terms
     */
    public List<String> expandQuery(String query) {
        Set<String> expandedTerms = new HashSet<>();
        
        // Split query into words
        String[] words = query.toLowerCase().split("\\s+");
        
        for (String word : words) {
            // Add the original word
            expandedTerms.add(word);
            
            // Add related terms
            expandedTerms.addAll(getRelatedTerms(word));
        }
        
        return new ArrayList<>(expandedTerms);
    }
    
    /**
     * Check if two terms are semantically related
     */
    public boolean areRelated(String term1, String term2) {
        String normalized1 = normalize(term1);
        String normalized2 = normalize(term2);
        
        if (normalized1.equals(normalized2)) {
            return true;
        }
        
        // Check if they're in the same semantic group
        for (Set<String> group : SEMANTIC_GROUPS.values()) {
            if (containsNormalized(group, normalized1) && containsNormalized(group, normalized2)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Normalize a term (lowercase, remove accents)
     */
    private String normalize(String term) {
        return term.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ýÿ]", "y")
                .replaceAll("[ç]", "c")
                .trim();
    }
    
    /**
     * Check if a set contains a term (normalized comparison)
     */
    private boolean containsNormalized(Set<String> set, String term) {
        String normalized = normalize(term);
        for (String item : set) {
            if (normalize(item).equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
