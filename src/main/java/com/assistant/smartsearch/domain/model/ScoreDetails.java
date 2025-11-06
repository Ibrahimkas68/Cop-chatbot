package com.assistant.smartsearch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScoreDetails {
    private double score;
    private List<String> matchedKeywords;
}
