package com.manish.search.search;

import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Tokenizer;
import com.manish.search.storage.Document;
import com.manish.search.storage.DocumentStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class SearchEngine {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final DocumentStore documentStore;

    public void index(Document document) {
        documentStore.save(document);

        String text = document.title() + " " + document.content();
        List<String> tokens = tokenizer.tokenize(text);

        for (String token : tokens) {
            invertedIndex.addToken(token, document.id());
        }
    }

    public List<SearchResult> search(String query) {
        List<String> tokens = tokenizer.tokenize(query);

        Map<String, Integer> scores = new HashMap<>();

        for(String token : tokens) {
            Set<String> documentIds = invertedIndex.search(token);

            for(String documentId : documentIds) {
                scores.merge(documentId, 1, Integer::sum);
            }
        }

        return scores.entrySet().stream()
                .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .toList();
    }
}
