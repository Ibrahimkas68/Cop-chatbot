package com.assistant.smartsearch.config;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Configuration class containing stop words for different languages.
 * Stop words are common words that are typically filtered out during text processing
 * as they don't carry significant meaning for search purposes.
 */
@Component
public class StopWordsConfig {

    /**
     * English stop words - common words that should be filtered out
     */
    private static final Set<String> ENGLISH_STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "be", "been",
            "have", "has", "had", "do", "does", "did", "will", "would", "should", "could",
            "may", "might", "must", "can", "this", "that", "these", "those", "it", "its",
            "i", "you", "he", "she", "we", "they", "my", "your", "his", "her", "our", "their",
            "what", "which", "who", "when", "where", "why", "how", "about", "up", "out",
            "if", "then", "than", "so", "no", "not", "only", "own", "same", "such", "here",
            "there", "each", "few", "more", "most", "other", "some", "time", "very",
            "said", "get", "make", "go", "see", "know", "take", "think", "come", "give",
            "look", "use", "find", "tell", "ask", "work", "seem", "feel", "try", "leave"
    );

    /**
     * French stop words - comprehensive list including articles, prepositions, pronouns, etc.
     */
    private static final Set<String> FRENCH_STOP_WORDS = Set.of(
            // Articles
            "le", "la", "les", "un", "une", "des", "du", "de", "d", "l",
            // Prepositions
            "dans", "pour", "avec", "sur", "sous", "entre", "vers", "chez", "sans", "par",
            "avant", "après", "pendant", "depuis", "jusqu", "contre", "selon", "malgré",
            // Pronouns
            "je", "tu", "il", "elle", "nous", "vous", "ils", "elles", "me", "te", "se",
            "lui", "leur", "en", "y", "qui", "que", "quoi", "dont", "où", "ce", "cela",
            "ça", "ceci", "celui", "celle", "ceux", "celles", "lequel", "laquelle",
            // Verbs
            "est", "sont", "était", "étaient", "être", "avoir", "as", "a", "avons",
            "avez", "ont", "avait", "avaient", "sera", "seront", "serait", "seraient",
            "fait", "faire", "dit", "dire", "va", "aller", "peut", "pouvoir", "doit", "devoir",
            // Conjunctions & Adverbs
            "et", "ou", "mais", "donc", "car", "ni", "soit", "alors", "ainsi", "aussi",
            "bien", "encore", "déjà", "jamais", "toujours", "souvent", "parfois", "très",
            "plus", "moins", "beaucoup", "peu", "assez", "trop", "tout", "tous", "toute", "toutes",
            // Others
            "si", "oui", "non", "ne", "pas", "point", "rien", "personne", "aucun", "aucune",
            "chaque", "chacun", "chacune", "autre", "autres", "même", "mêmes", "tel", "telle","mes","comment"
    );

    /**
     * Arabic stop words - comprehensive list including particles, pronouns, prepositions, etc.
     */
    private static final Set<String> ARABIC_STOP_WORDS = Set.of(
            // Articles and particles
            "ال", "في", "من", "إلى", "على", "عن", "مع", "بعد", "قبل", "تحت", "فوق", "أمام", "خلف",
            "بين", "ضد", "حول", "خلال", "عبر", "نحو", "لدى", "عند", "غير", "سوى", "إلا",
            // Pronouns
            "أنا", "أنت", "أنتم", "أنتن", "هو", "هي", "هم", "هن", "نحن", "إياي", "إياك", "إياه", "إياها",
            "إيانا", "إياكم", "إياكن", "إياهم", "إياهن", "هذا", "هذه", "ذلك", "تلك", "هؤلاء", "أولئك",
            "ما", "التي", "الذي", "اللذان", "اللتان", "الذين", "اللاتي", "اللواتي",
            // Verbs (common auxiliary and linking verbs)
            "كان", "كانت", "كانوا", "كن", "يكون", "تكون", "أكون", "نكون", "تكونوا", "يكن",
            "ليس", "ليست", "لسنا", "لست", "لسن", "لسوا", "صار", "صارت", "أصبح", "أصبحت",
            "بات", "باتت", "ظل", "ظلت", "مازال", "مازالت", "لايزال", "لاتزال",
            // Conjunctions and connectors
            "و", "أو", "لكن", "إذا", "إذ", "حيث", "بينما", "كما", "مثل", "كأن", "لأن", "حتى",
            "لو", "لولا", "لوما", "كي", "لكي", "إن", "أن", "ليت", "لعل", "عسى",
            // Prepositions and particles
            "ب", "ل", "ك", "س", "ف", "قد", "لقد", "لم", "لن", "لا",
            "بل", "نعم", "كلا", "أجل", "حقا", "فعلا", "طبعا", "أيضا", "كذلك", "هكذا", "هنا", "هناك",
            "هنالك", "أين", "كيف", "متى", "لماذا", "ماذا", "أي", "أية", "كم", "كأين",
            // Common words
            "كل", "بعض", "جميع", "معظم", "أكثر", "أقل", "أول", "آخر", "نفس", "ذات",
            "فقط", "منذ", "مذ", "خاصة", "عامة"
    );

    /**
     * Important short words/acronyms that should NOT be filtered out despite being short
     */
    private static final Set<String> IMPORTANT_SHORT_WORDS = Set.of(
            // Technical acronyms
            "ai", "ml", "it", "ui", "ux", "ar", "vr", "3d", "2d", "io", "os", "db", "id",
            "api", "url", "css", "js", "php", "sql", "xml", "json", "http", "ftp", "ssh",
            "aws", "gcp", "cdn", "seo", "crm", "erp", "bi", "ci", "cd", "qa", "dev", "ops",
            // Common abbreviations
            "usa", "uk", "eu", "uae", "ksa", "gcc", "mena", "ceo", "cto", "cfo", "hr", "pr"
    );

    /**
     * Returns all stop words combined from all languages
     * @return Set of all stop words
     */
    public Set<String> getAllStopWords() {
        return Set.of(
                ENGLISH_STOP_WORDS,
                FRENCH_STOP_WORDS,
                ARABIC_STOP_WORDS
        ).stream()
                .flatMap(Set::stream)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Returns English stop words only
     * @return Set of English stop words
     */
    public Set<String> getEnglishStopWords() {
        return ENGLISH_STOP_WORDS;
    }

    /**
     * Returns French stop words only
     * @return Set of French stop words
     */
    public Set<String> getFrenchStopWords() {
        return FRENCH_STOP_WORDS;
    }

    /**
     * Returns Arabic stop words only
     * @return Set of Arabic stop words
     */
    public Set<String> getArabicStopWords() {
        return ARABIC_STOP_WORDS;
    }

    /**
     * Returns important short words that should not be filtered
     * @return Set of important short words/acronyms
     */
    public Set<String> getImportantShortWords() {
        return IMPORTANT_SHORT_WORDS;
    }

    /**
     * Checks if a word is a stop word in any language
     * @param word The word to check
     * @return true if the word is a stop word, false otherwise
     */
    public boolean isStopWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return true;
        }
        return getAllStopWords().contains(word.toLowerCase().trim());
    }

    /**
     * Checks if a word is an important short word that should be preserved
     * @param word The word to check
     * @return true if the word is important and should not be filtered, false otherwise
     */
    public boolean isImportantShortWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return IMPORTANT_SHORT_WORDS.contains(word.toLowerCase().trim());
    }
}
