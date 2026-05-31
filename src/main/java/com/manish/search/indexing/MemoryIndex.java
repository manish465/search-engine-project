package com.manish.search.indexing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MemoryIndex implements SearchableIndex {
    private final Map<String, List<Posting>> index = new ConcurrentHashMap<>();
    @Getter
    private final Set<String> vocabulary = ConcurrentHashMap.newKeySet();

    public void clear() {
        index.clear();
    }

    public void addToken(
            String token,
            String documentId,
            FieldType field,
            int position
    ) {
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

    @Override
    public List<Posting> getPostings(String token) {
        return index.getOrDefault(token, Collections.emptyList());
    }

    public Map<String, List<Posting>> exportIndex() {
        return index;
    }

    @Override
    public int documentFrequency(String term) {
        return getPostings(term).size();
    }
}
