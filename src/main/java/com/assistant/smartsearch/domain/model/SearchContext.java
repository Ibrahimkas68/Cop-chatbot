package com.assistant.smartsearch.domain.model;

import lombok.Data;

@Data
public class SearchContext {
    private String userId;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String location;
}
