package com.manish.search.service;

import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Posting;
import com.manish.search.indexing.Tokenizer;
import com.manish.search.search.BM25Scorer;
import com.manish.search.search.DocumentStatisticsStore;
import com.manish.search.search.DocumentStats;
import com.manish.search.search.SearchResult;
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
    private final DocumentStatisticsStore documentStatisticsStore;
    private final BM25Scorer bm25Scorer;

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
        Map<String, Double> scores = new HashMap<>();
        int totalDocs = documentStatisticsStore.totalDocuments();
        double avgDocLength = documentStatisticsStore.averageDocumentLength();

        for(String token : tokens) {
            int df = invertedIndex.documentFrequency(token);
            List<Posting> postings = invertedIndex.getPostings(token);

            for(Posting posting : postings) {
                String documentId = posting.getDocumentId();
                int tf = posting.getTermFrequency();
                int docLength = documentStatisticsStore.get(documentId).getLength();

                double score = bm25Scorer.score(tf, df, totalDocs, docLength, avgDocLength);

                scores.merge(documentId, score, Double::sum);
            }
        }

        return scores.entrySet().stream()
                .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();
    }
}
