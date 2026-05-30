package com.manish.search.indexing;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvertedIndex {
    private final Map<String, List<Posting>> index;

    public InvertedIndex() {
        index = new ConcurrentHashMap<>();
    }

    public void addToken(String token, String documentId, int position) {
        List<Posting> postings = index.computeIfAbsent(token, k -> new ArrayList<>());

        Posting posting = postings.stream()
                .filter(p -> p.getDocumentId().equals(documentId))
                .findFirst()
                .orElse(null);

        if(posting == null) {
            posting = new Posting(documentId);
            postings.add(posting);
        }
    }

    public List<Posting> getPostings(String token) {
        return index.getOrDefault(token, Collections.emptyList());
    }
}
