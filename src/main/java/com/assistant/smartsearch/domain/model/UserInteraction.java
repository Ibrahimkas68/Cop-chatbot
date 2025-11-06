package com.assistant.smartsearch.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class UserInteraction {
    private Long clickedDocumentId;
    private int clickedPosition;
    private double timeToClickSeconds;
    private List<Long> viewedResults;
    private double timeSpentOnResultSeconds;
    private boolean wasHelpful;
    private int feedbackRating;
}
