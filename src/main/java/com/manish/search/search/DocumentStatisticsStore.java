package com.manish.search.search;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentStatisticsStore {
    private final Map<String, DocumentStats> stats;

    public DocumentStatisticsStore() {
        stats = new ConcurrentHashMap<>();
    }

    public void save(DocumentStats stat) {
        stats.put(stat.getDocumentId(), stat);
    }

    public DocumentStats get(String documentId) {
        return stats.get(documentId);
    }
}
