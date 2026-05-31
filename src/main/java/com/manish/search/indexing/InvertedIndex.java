package com.manish.search.indexing;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvertedIndex {
    private final Map<String, List<Posting>> index;
    @Getter
    private final Set<String> vocabulary;

    public InvertedIndex() {
        index = new ConcurrentHashMap<>();
        vocabulary = ConcurrentHashMap.newKeySet();
    }

    public void addToken(String token, String documentId, FieldType field, int position) {
        vocabulary.add(token);

        List<Posting> postings = index.computeIfAbsent(token, k -> new ArrayList<>());

        Posting posting = postings.stream()
                .filter(p -> p.getDocumentId().equals(documentId) && p.getField() == field)
                .findFirst()
                .orElse(null);

        if(posting == null) {
            posting = new Posting(documentId, field);
            postings.add(posting);
        }

        posting.addPosition(position);
    }

    public List<Posting> getPostings(String token) {
        return index.getOrDefault(token, Collections.emptyList());
    }

    public int documentFrequency(String term) {
        return getPostings(term).size();
    }
}
