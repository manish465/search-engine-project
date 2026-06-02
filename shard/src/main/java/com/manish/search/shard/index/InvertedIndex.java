package com.manish.search.shard.index;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvertedIndex {
    private final Map<String, List<Posting>> index = new ConcurrentHashMap<>();

    public void add(String term, String documentId, int position) {
        List<Posting> postings = index.computeIfAbsent(term, t -> new ArrayList<>());
        Posting posting = postings
                .stream()
                .filter(p -> p.getDocumentId().equals(documentId))
                .findFirst()
                .orElse(null);

        if(posting == null) {
            posting = new Posting(documentId);
            postings.add(posting);
        }

        posting.addPosition(position);
    }

    public List<Posting> postings(String term) {
        return index.getOrDefault(term, List.of());
    }
}
