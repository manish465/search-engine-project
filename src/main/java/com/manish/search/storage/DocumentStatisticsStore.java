package com.manish.search.storage;

import com.manish.search.model.DocumentStats;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentStatisticsStore {
    private final Map<String, DocumentStats> stats;

    public DocumentStatisticsStore() {
        stats = new ConcurrentHashMap<>();
    }

    public void save(DocumentStats stat) {
        stats.put(stat.documentId(), stat);
    }

    public DocumentStats get(String documentId) {
        return stats.get(documentId);
    }

    public int totalDocuments() {
        return stats.size();
    }

    public double averageDocumentLength() {
        if(stats.isEmpty()) return 0;

        int totalLength = stats.values()
                .stream().mapToInt(DocumentStats::length)
                .sum();

        return (double) totalLength / stats.size();
    }

    public void remove(String documentId) {
        stats.remove(documentId);
    }
}
