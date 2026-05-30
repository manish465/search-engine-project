package com.manish.search.search;

import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Posting;
import com.manish.search.indexing.Tokenizer;
import com.manish.search.model.WeightedQuery;
import com.manish.search.ranking.BM25Scorer;
import com.manish.search.storage.DocumentStatisticsStore;
import com.manish.search.model.DocumentStats;
import com.manish.search.model.SearchResult;
import com.manish.search.model.Document;
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

    public List<SearchResult> search(WeightedQuery query) {
        Map<String, Double> positive = new HashMap<>();
        Map<String, Double> negative = new HashMap<>();

        int totalDocs = documentStatisticsStore.totalDocuments();
        double avgDocLength = documentStatisticsStore.averageDocumentLength();

        scoreTerms(query.positiveTerms(), positive, totalDocs, avgDocLength);
        scoreTerms(query.negativeTerms(), negative, totalDocs, avgDocLength);

        Set<String> allDocs = new HashSet<>();
        allDocs.addAll(positive.keySet());
        allDocs.addAll(negative.keySet());

        List<SearchResult> results = new ArrayList<>();
        double penaltyWeight = 0.7;

        for(String docId : allDocs) {
            double positiveScore = positive.getOrDefault(docId, 0.0);
            double negativeScore = negative.getOrDefault(docId, 0.0);

            double finalScore = positiveScore - (negativeScore * penaltyWeight);

            results.add(new SearchResult(docId, positiveScore, negativeScore, finalScore));
        }

        results.sort(Comparator.comparingDouble(SearchResult::finalScore).reversed());

        return results;
    }

    private void scoreTerms(
            List<String> terms,
            Map<String, Double> scores,
            int totalDocs,
            double avgDocLength
    ) {
        for(String term : terms) {
            int df = invertedIndex.documentFrequency(term);
            List<Posting> postings = invertedIndex.getPostings(term);

            for(Posting posting : postings) {
                String docId = posting.getDocumentId();
                int tf = posting.getTermFrequency();
                int docLength = documentStatisticsStore.get(docId).length();

                double score = bm25Scorer.score(tf, df, totalDocs, docLength, avgDocLength);
                scores.merge(docId, score, Double::sum);
            }
        }
    }
}
