package com.assistant.smartsearch.application;

import com.assistant.smartsearch.domain.port.KeywordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentProcessorService {

    private final KeywordExtractor keywordExtractor;

    @Autowired
    public DocumentProcessorService(KeywordExtractor keywordExtractor) {
        this.keywordExtractor = keywordExtractor;
    }
    
    /**
     * Process a document into a bag-of-words representation
     * @param content The document content
     * @return Map of terms to their frequencies in the document
     */
    public Map<String, Integer> processDocument(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Extract keywords
        List<String> keywords = keywordExtractor.extractKeywords(content);
        
        // Count term frequencies
        Map<String, Integer> termFrequencies = new HashMap<>();
        for (String term : keywords) {
            termFrequencies.put(term, termFrequencies.getOrDefault(term, 0) + 1);
        }
        
        return termFrequencies;
    }
    
    /**
     * Convert a document to a vector representation based on term frequencies
     * @param document The document content or pre-processed terms
     * @param vocabulary The global vocabulary to use for vectorization
     * @return A vector where each element corresponds to a term in the vocabulary
     */
    public double[] vectorizeDocument(String document, Set<String> vocabulary) {
        Map<String, Integer> termFrequencies = processDocument(document);
        return vectorizeTermFrequencies(termFrequencies, vocabulary);
    }
    
    /**
     * Convert term frequencies to a vector based on a global vocabulary
     */
    public double[] vectorizeTermFrequencies(Map<String, Integer> termFrequencies, Set<String> vocabulary) {
        double[] vector = new double[vocabulary.size()];
        List<String> vocabList = new ArrayList<>(vocabulary);
        
        for (int i = 0; i < vocabList.size(); i++) {
            String term = vocabList.get(i);
            vector[i] = termFrequencies.getOrDefault(term, 0);
        }
        
        return vector;
    }
    
    /**
     * Build a global vocabulary from a collection of documents
     */
    public Set<String> buildVocabulary(Collection<String> documents) {
        return documents.stream()
            .flatMap(doc -> processDocument(doc).keySet().stream())
            .collect(Collectors.toSet());
    }
    
    /**
     * Apply TF-IDF weighting to a collection of document vectors
     */
    public double[][] applyTfIdf(double[][] documentVectors) {
        if (documentVectors == null || documentVectors.length == 0) {
            return documentVectors;
        }
        
        int numDocs = documentVectors.length;
        int numTerms = documentVectors[0].length;
        double[][] tfidfVectors = new double[numDocs][numTerms];
        
        // Calculate document frequencies (number of documents containing each term)
        int[] docFrequencies = new int[numTerms];
        for (int i = 0; i < numDocs; i++) {
            for (int j = 0; j < numTerms; j++) {
                if (documentVectors[i][j] > 0) {
                    docFrequencies[j]++;
                }
            }
        }
        
        // Calculate TF-IDF weights
        for (int i = 0; i < numDocs; i++) {
            // Calculate document vector length for normalization
            double docLength = 0;
            for (int j = 0; j < numTerms; j++) {
                double tf = documentVectors[i][j];
                double idf = Math.log((double) numDocs / (1 + docFrequencies[j]));
                tfidfVectors[i][j] = tf * idf;
                docLength += tfidfVectors[i][j] * tfidfVectors[i][j];
            }
            
            // Normalize the vector
            docLength = Math.sqrt(docLength);
            if (docLength > 0) {
                for (int j = 0; j < numTerms; j++) {
                    tfidfVectors[i][j] /= docLength;
                }
            }
        }
        
        return tfidfVectors;
    }
    
    /**
     * Preprocess text for LDA (lowercase, remove punctuation, etc.)
     */
    public String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        
        // Convert to lowercase
        String processed = text.toLowerCase();
        
        // Remove punctuation (keep only letters, numbers, and whitespace)
        processed = processed.replaceAll("[^a-z0-9\\s]", " ");
        
        // Replace multiple whitespace with single space
        processed = processed.replaceAll("\\s+", " ").trim();
        
        return processed;
    }
}
