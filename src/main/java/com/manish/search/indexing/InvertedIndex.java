package com.manish.search.indexing;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvertedIndex {
    private final Map<String, Set<String>> index;

    public InvertedIndex() {
        index = new ConcurrentHashMap<>();
    }

    public void addToken(String token, String documentId) {
        index.computeIfAbsent(
                token,
                k -> ConcurrentHashMap.newKeySet()
        ).add(documentId);
    }

    public Set<String> search(String token) {
        return index.getOrDefault(token, Collections.emptySet());
    }
}
