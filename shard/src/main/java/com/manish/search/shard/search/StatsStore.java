package com.manish.search.shard.search;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatsStore {
    private final Map<String,DocumentStats> stats = new ConcurrentHashMap<>();

    public void save(DocumentStats stat) {
        stats.put(stat.documentId(), stat);
    }

    public DocumentStats get(String docId) {
        return stats.get(docId);
    }

    public int totalDocuments() {
        return stats.size();
    }

    public double averageLength() {
        return stats
                .values()
                .stream()
                .mapToInt(DocumentStats::length)
                .average()
                .orElse(0);
    }
}
