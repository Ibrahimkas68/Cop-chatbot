package com.assistant.smartsearch.infrastructure.service;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Real LDA Topic Modeling Service using MALLET library.
 * Trains on actual database content to discover topics.
 */
@Slf4j
@Service
public class LDATopicModelService {
    
    private static final int NUM_TOPICS = 10;
    private static final int NUM_ITERATIONS = 1000;
    private static final int NUM_THREADS = 4;
    private static final int TOP_WORDS_PER_TOPIC = 10;
    
    private ParallelTopicModel model;
    private InstanceList instances;
    private TopicInferencer inferencer;
    private Alphabet alphabet;
    private boolean modelTrained = false;
    private final Object modelLock = new Object();
    
    // Cache for document topic distributions
    private final Map<String, double[]> documentTopicCache = new ConcurrentHashMap<>();
    
    /**
     * Train the LDA model on a collection of documents
     * @param documents Map of document ID to content
     */
    public void trainModel(Map<String, String> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("Cannot train model: no documents provided");
            return;
        }
        
        synchronized (modelLock) {
            try {
                log.info("Starting LDA model training with {} documents", documents.size());
                
                // Create a pipe for text preprocessing
                Pipe pipe = createPipe();
                
                // Convert documents to instances
                List<Instance> documentInstances = new ArrayList<>();
                for (Map.Entry<String, String> entry : documents.entrySet()) {
                    Instance instance = new Instance(
                        entry.getValue(),
                        null,
                        entry.getKey(),
                        null
                    );
                    documentInstances.add(instance);
                }
                
                // Create instance list and process through pipe
                instances = new InstanceList(pipe);
                instances.addThruPipe(new ArrayIterator(documentInstances));
                
                log.info("Created instance list with {} instances, vocabulary size: {}", 
                    instances.size(), instances.getDataAlphabet().size());
                
                // Create and train the model
                model = new ParallelTopicModel(NUM_TOPICS, 1.0, 0.01);
                model.addInstances(instances);
                model.setNumIterations(NUM_ITERATIONS);
                model.setNumThreads(NUM_THREADS);
                
                log.info("Training LDA model with {} topics...", NUM_TOPICS);
                model.estimate();
                
                // Create inferencer for new documents
                inferencer = model.getInferencer();
                alphabet = instances.getDataAlphabet();
                
                modelTrained = true;
                documentTopicCache.clear();
                
                log.info("LDA model training completed successfully");
                logTopics();
                
            } catch (Exception e) {
                log.error("Error training LDA model", e);
                modelTrained = false;
            }
        }
    }
    
    /**
     * Get topic distribution for a given document
     * @param documentContent The document content
     * @return Array of topic probabilities (one per topic)
     */
    public double[] inferDocumentTopics(String documentContent) {
        if (!modelTrained || inferencer == null) {
            log.warn("Model not trained yet, cannot infer topics");
            return new double[NUM_TOPICS];
        }
        
        try {
            // Create instance from document
            Instance instance = new Instance(
                documentContent,
                null,
                "infer",
                null
            );
            
            // Process through the same pipe that was used for training
            Pipe pipe = createPipe();
            instance = pipe.instanceFrom(instance);
            
            // Infer topics
            double[] topicDistribution = inferencer.getSampledDistribution(instance, 100, 10, 5);
            
            return topicDistribution;
        } catch (Exception e) {
            log.error("Error inferring topics for document", e);
            return new double[NUM_TOPICS];
        }
    }
    
    /**
     * Calculate similarity between two documents based on their topic distributions
     * @param doc1Content First document content
     * @param doc2Content Second document content
     * @return Similarity score between 0 and 1
     */
    public double calculateDocumentSimilarity(String doc1Content, String doc2Content) {
        if (!modelTrained) {
            return 0.0;
        }
        
        double[] topics1 = inferDocumentTopics(doc1Content);
        double[] topics2 = inferDocumentTopics(doc2Content);
        
        return calculateCosineSimilarity(topics1, topics2);
    }
    
    /**
     * Find similar documents based on topic similarity
     * @param queryContent Query document content
     * @param candidateDocuments Map of document ID to content
     * @param topN Number of top similar documents to return
     * @return Sorted list of document IDs with similarity scores
     */
    public List<Map.Entry<String, Double>> findSimilarDocuments(
            String queryContent, 
            Map<String, String> candidateDocuments,
            int topN) {
        
        if (!modelTrained || candidateDocuments == null || candidateDocuments.isEmpty()) {
            return new ArrayList<>();
        }
        
        double[] queryTopics = inferDocumentTopics(queryContent);
        
        return candidateDocuments.entrySet().stream()
            .map(entry -> {
                double[] docTopics = inferDocumentTopics(entry.getValue());
                double similarity = calculateCosineSimilarity(queryTopics, docTopics);
                return new AbstractMap.SimpleEntry<>(entry.getKey(), similarity);
            })
            .filter(entry -> entry.getValue() > 0.1) // Filter out very low similarities
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(topN)
            .collect(Collectors.toList());
    }
    
    /**
     * Get the top words for each topic
     * @return Map of topic ID to list of top words
     */
    public Map<Integer, List<String>> getTopicsTerms() {
        if (!modelTrained || model == null) {
            return new HashMap<>();
        }
        
        Map<Integer, List<String>> topicsMap = new HashMap<>();
        
        try {
            Object[][] topWords = model.getTopWords(TOP_WORDS_PER_TOPIC);
            
            for (int topic = 0; topic < topWords.length; topic++) {
                List<String> words = new ArrayList<>();
                for (Object word : topWords[topic]) {
                    if (word != null) {
                        words.add(word.toString());
                    }
                }
                topicsMap.put(topic, words);
            }
        } catch (Exception e) {
            log.error("Error getting top words for topics", e);
        }
        
        return topicsMap;
    }
    
    /**
     * Get model status information
     * @return Map containing model metadata
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (!modelTrained || model == null) {
            info.put("status", "No model trained yet");
            info.put("trained", false);
            return info;
        }
        
        info.put("trained", true);
        info.put("numDocuments", instances != null ? instances.size() : 0);
        info.put("numTopics", NUM_TOPICS);
        info.put("vocabularySize", alphabet != null ? alphabet.size() : 0);
        info.put("topics", getTopicsTerms());
        
        return info;
    }
    
    /**
     * Check if model is trained
     */
    public boolean isModelTrained() {
        return modelTrained;
    }
    
    /**
     * Create preprocessing pipe for text
     */
    private Pipe createPipe() {
        ArrayList<Pipe> pipes = new ArrayList<>();
        
        // 1. Convert text to lowercase
        pipes.add(new CharSequenceLowercase());
        
        // 2. Tokenize
        pipes.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]*\\p{L}")));
        
        // 3. Remove common stop words
        pipes.add(new TokenSequenceRemoveStopwords(false, false));
        
        // 4. Convert to feature sequence (bag of words)
        pipes.add(new TokenSequence2FeatureSequence());
        
        return new SerialPipes(pipes);
    }
    
    /**
     * Calculate cosine similarity between two vectors
     */
    private double calculateCosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Log top words for each topic
     */
    private void logTopics() {
        Map<Integer, List<String>> topics = getTopicsTerms();
        log.info("=== Discovered Topics ===");
        topics.forEach((topicId, words) -> 
            log.info("Topic {}: {}", topicId, String.join(", ", words))
        );
    }
    
    /**
     * Clear the model and cache
     */
    public void clearModel() {
        synchronized (modelLock) {
            model = null;
            instances = null;
            inferencer = null;
            alphabet = null;
            modelTrained = false;
            documentTopicCache.clear();
            log.info("Model cleared");
        }
    }
}
