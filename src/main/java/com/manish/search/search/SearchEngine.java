package com.manish.search.search;

import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Posting;
import com.manish.search.indexing.Tokenizer;
import com.manish.search.storage.Document;
import com.manish.search.storage.DocumentStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchEngine {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final DocumentStore documentStore;
    private final DocumentStatisticsStore documentStatisticsStore;

    public void index(Document document) {
        documentStore.save(document);

        String text = document.title() + " " + document.content();
        List<String> tokens = tokenizer.tokenize(text);

        for (int i = 0; i < tokens.size(); i++) {
            invertedIndex.addToken(tokens.get(i), document.id(), i);
        }

        documentStatisticsStore.save(
                new DocumentStats(document.id(), tokens.size())
        );
    }

    public List<SearchResult> search(String query) {
        List<String> tokens = tokenizer.tokenize(query);

        Map<String, Integer> scores = new HashMap<>();

        for(String token : tokens) {
            Set<String> documentIds = invertedIndex
                    .getPostings(token)
                    .stream()
                    .map(Posting::getDocumentId)
                    .collect(Collectors.toSet());

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
