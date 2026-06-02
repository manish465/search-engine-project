package com.manish.search.shard.service;

import com.manish.search.common.dto.Document;
import com.manish.search.common.dto.SearchResult;
import com.manish.search.shard.index.InvertedIndex;
import com.manish.search.shard.index.Posting;
import com.manish.search.shard.index.Tokenizer;
import com.manish.search.shard.search.BM25Scorer;
import com.manish.search.shard.search.DocumentStats;
import com.manish.search.shard.search.StatsStore;
import com.manish.search.shard.storage.DocumentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocalSearchEngine {
    private final InvertedIndex index;
    private final Tokenizer tokenizer;
    private final DocumentStore store;
    private final StatsStore statsStore;
    private final BM25Scorer bm25;

    public void indexDocument(Document document) {
        store.save(document);

        List<String> terms = tokenizer.tokenize(document.title() + " " + document.content());

        for(int i = 0; i < terms.size(); i++) {
            index.add(terms.get(i), document.id(), i);
        }

        statsStore.save(new DocumentStats(document.id(), terms.size()));
    }

    public List<SearchResult> search(String query) {
        List<String> terms = tokenizer.tokenize(query);
        Map<String,Double> scores = new HashMap<>();
        int totalDocs = statsStore.totalDocuments();
        double avgLength = statsStore.averageLength();

        for(String term : terms) {
            List<Posting> postings = index.postings(term);
            int df = postings.size();

            for(Posting posting : postings) {
                String docId = posting.getDocumentId();
                int docLength = statsStore.get(docId).length();
                double score = bm25.score(posting.getFrequency(), df, totalDocs, docLength, avgLength);

                scores.merge(docId, score, Double::sum);
            }
        }

        return scores.entrySet()
                .stream()
                .map(e -> new SearchResult(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .toList();
    }
}
