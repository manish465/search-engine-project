package com.manish.search.search;

import com.manish.search.indexing.FieldType;
import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Posting;
import com.manish.search.indexing.Tokenizer;
import com.manish.search.model.*;
import com.manish.search.ranking.BM25Scorer;
import com.manish.search.ranking.ScoreExplanation;
import com.manish.search.ranking.SearchResult;
import com.manish.search.storage.DocumentStatisticsStore;
import com.manish.search.storage.DocumentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchEngine {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final DocumentStore documentStore;
    private final DocumentStatisticsStore documentStatisticsStore;
    private final BM25Scorer bm25Scorer;
    private final PhraseMatcher phraseMatcher;

    public void index(Document document) {
        documentStore.save(document);

        int totalLength = 0;
        totalLength += indexField(document.id(), document.title(), FieldType.TITLE);
        totalLength += indexField(document.id(), document.content(), FieldType.CONTENT);
        if(document.tags() != null && !document.tags().isEmpty())
            for(String tags : document.tags())
                totalLength += indexField(document.id(), tags, FieldType.TAG);

        documentStatisticsStore.save(new DocumentStats(document.id(), totalLength));
    }

    public List<SearchResult> search(WeightedQuery query) {
        Map<String, Double> positiveScores = new HashMap<>();
        Map<String, Double> negativeScores = new HashMap<>();

        Map<String, List<ScoreExplanation>> documentExplanations = new HashMap<>();

        int totalDocs = documentStatisticsStore.totalDocuments();
        double avgDocLength = documentStatisticsStore.averageDocumentLength();

        scoreTerms(query.positiveTerms(), positiveScores, totalDocs, avgDocLength, documentExplanations);
        scoreTerms(query.negativeTerms(), negativeScores, totalDocs, avgDocLength, documentExplanations);
        scorePhrases(query.positiveTerms(), positiveScores, documentExplanations);
        scorePhrases(query.negativeTerms(), negativeScores, documentExplanations);

        Set<String> allDocs = new HashSet<>();
        allDocs.addAll(positiveScores.keySet());
        allDocs.addAll(negativeScores.keySet());

        List<SearchResult> results = new ArrayList<>();
        double penaltyWeight = 0.7;

        for(String docId : allDocs) {
            double positiveScore = positiveScores.getOrDefault(docId, 0.0);
            double negativeScore = negativeScores.getOrDefault(docId, 0.0);

            double finalScore = positiveScore - (negativeScore * penaltyWeight);

            results.add(new SearchResult(
                    docId,
                    positiveScore,
                    negativeScore,
                    finalScore,
                    documentExplanations.getOrDefault(docId, List.of()))
            );
        }

        results.sort(Comparator.comparingDouble(SearchResult::finalScore).reversed());

        return results;
    }

    private void scoreTerms(
            List<String> terms,
            Map<String, Double> scores,
            int totalDocs,
            double avgDocLength,
            Map<String, List<ScoreExplanation>> documentExplanations
    ) {

        for(String term : terms) {
            int df = invertedIndex.documentFrequency(term);
            List<Posting> postings = invertedIndex.getPostings(term);

            for(Posting posting : postings) {
                String documentId = posting.getDocumentId();
                DocumentStats stats = documentStatisticsStore.get(documentId);

                if(stats == null) continue;

                int tf = posting.getTermFrequency();
                int docLength = stats.length();

                double bm25corer = bm25Scorer.score(tf, df, totalDocs, docLength, avgDocLength);
                double boost = FieldBoosts.BOOSTS.getOrDefault(posting.getField(), 1.0);
                double score = bm25corer * boost;

                documentExplanations
                        .computeIfAbsent(documentId, d -> new ArrayList<>())
                        .add(new ScoreExplanation(term, posting.getField().name(), bm25corer, boost, score));

                scores.merge(documentId, score, Double::sum);
            }
        }
    }

    private int indexField(String documentId, String text, FieldType field) {
        List<String> tokens = tokenizer.tokenize(text);

        for(int i=0; i < tokens.size(); i++) {
            invertedIndex.addToken(tokens.get(i), documentId, field, i);
        }

        return tokens.size();
    }

    private void scorePhrases(
            List<String> phrases,
            Map<String, Double> scores,
            Map<String, List<ScoreExplanation>> documentExplanations
    ) {
        final double PHRASE_BOOST = 10.0;

        for(String phrase : phrases) {
            List<String> terms = tokenizer.tokenize(phrase);

            if(terms.isEmpty()) continue;

            Map<String, List<Posting>> postingsByDoc = new HashMap<>();
            boolean firstTerm = true;

            for (String term : terms) {
                List<Posting> postings = invertedIndex.getPostings(term);

                if(firstTerm) {
                    for (Posting posting : postings) {
                        postingsByDoc.put(posting.getDocumentId(), new ArrayList<>(List.of(posting)));
                    }

                    firstTerm = false;
                    continue;
                }

                postingsByDoc.entrySet()
                        .removeIf(entry -> {
                            Posting matchingPosting = postings
                                    .stream()
                                    .filter(p -> p.getDocumentId().equals(entry.getKey()))
                                    .findFirst()
                                    .orElse(null);

                            if(matchingPosting == null) return true;

                            entry.getValue().add(matchingPosting);
                            return false;
                        });
            }

            for(var entry : postingsByDoc.entrySet()) {
                if(phraseMatcher.matches(entry.getValue())) {
                    scores.merge(entry.getKey(), PHRASE_BOOST, Double::sum);
                    documentExplanations
                            .computeIfAbsent(
                                    entry.getKey(),
                                    d -> new ArrayList<>())
                            .add(new ScoreExplanation(
                                    "\"" + phrase + "\"",
                                    "PHRASE",
                                    1.0,
                                    PHRASE_BOOST,
                                    PHRASE_BOOST
                            ));
                }
            }
        }
    }
}
