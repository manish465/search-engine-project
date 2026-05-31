package com.manish.search.search;

import com.manish.search.fuzzy.FuzzyTermFinder;
import com.manish.search.indexing.*;
import com.manish.search.model.*;
import com.manish.search.ranking.BM25Scorer;
import com.manish.search.ranking.ScoreExplanation;
import com.manish.search.ranking.SearchResult;
import com.manish.search.storage.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SearchEngine {
    private final Tokenizer tokenizer;
    private final DocumentStore documentStore;
    private final DocumentStatisticsStore documentStatisticsStore;
    private final BM25Scorer bm25Scorer;
    private final PhraseMatcher phraseMatcher;
    private final FuzzyTermFinder fuzzyTermFinder;
    private final SegmentWriter segmentWriter;
    private final SegmentManager segmentManager;
    private final MemoryIndex memoryIndex;
    private final SegmentReader segmentReader;
    private final CompositeIndex compositeIndex;

    private final Counter searchCounter;
    private final Counter indexedCounter;
    private final Timer searchTimer;

    private final List<Document> buffer = Collections.synchronizedList(new ArrayList<>());
    private final ReentrantLock flushLock = new ReentrantLock();
    private static final int FLUSH_THRESHOLD = 1000;
    private final Map<String, List<String>> fuzzyCache = new ConcurrentHashMap<>();
    private final Set<String> tombstones = ConcurrentHashMap.newKeySet();

    public SearchEngine(
            Tokenizer tokenizer,
            DocumentStore documentStore,
            DocumentStatisticsStore documentStatisticsStore,
            BM25Scorer bm25Scorer,
            PhraseMatcher phraseMatcher,
            FuzzyTermFinder fuzzyTermFinder,
            SegmentWriter segmentWriter,
            SegmentManager segmentManager,
            MeterRegistry meterRegistry,
            MemoryIndex memoryIndex, SegmentReader segmentReader, CompositeIndex compositeIndex
    ) {
        this.tokenizer = tokenizer;
        this.documentStore = documentStore;
        this.documentStatisticsStore = documentStatisticsStore;
        this.bm25Scorer = bm25Scorer;
        this.phraseMatcher = phraseMatcher;
        this.fuzzyTermFinder = fuzzyTermFinder;
        this.segmentWriter = segmentWriter;
        this.segmentManager = segmentManager;
        this.memoryIndex = memoryIndex;
        this.segmentReader = segmentReader;
        this.compositeIndex = compositeIndex;


        this.searchCounter = meterRegistry.counter("search.requests");
        this.indexedCounter = meterRegistry.counter("documents.indexed");
        this.searchTimer = meterRegistry.timer("search.latency");
    }

    public void index(Document document) {
        indexedCounter.increment();

        documentStore.save(document);
        buffer.add(document);
        indexDocument(document);
        if(buffer.size() >= FLUSH_THRESHOLD) flushSegment();
    }

    public List<SearchResult> search(WeightedQuery query) {
        Timer.Sample sample = Timer.start();

        try {
            searchCounter.increment();

            Map<String, Double> positiveScores = new HashMap<>();
            Map<String, Double> negativeScores = new HashMap<>();

            Map<String, List<ScoreExplanation>> documentExplanations = new HashMap<>();

            int totalDocs = documentStatisticsStore.totalDocuments();
            double avgDocLength = documentStatisticsStore.averageDocumentLength();

            if(avgDocLength <= 0) avgDocLength = 1;

            scoreTerms(query.positiveTerms(), positiveScores, totalDocs, avgDocLength, documentExplanations);
            scoreTerms(query.negativeTerms(), negativeScores, totalDocs, avgDocLength, documentExplanations);
            scorePhrases(query.positivePhrases(), positiveScores, documentExplanations);
            scorePhrases(query.negativePhrases(), positiveScores, documentExplanations);

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

        } finally {
            sample.stop(searchTimer);
        }
    }

    private void scoreTerms(
            List<String> terms,
            Map<String, Double> scores,
            int totalDocs,
            double avgDocLength,
            Map<String, List<ScoreExplanation>> documentExplanations
    ) {
        for(String originalTerm : terms) {
            Set<String> searchTerms = new LinkedHashSet<>();

            searchTerms.add(originalTerm);

            List<String> fuzzyTerms = fuzzyCache.computeIfAbsent(
                    originalTerm, fuzzyTermFinder::findSimilarTerms
            );

            searchTerms.addAll(fuzzyTerms.stream()
                            .filter(term -> !term.equals(originalTerm))
                            .toList()
            );

            for(String searchTerm : searchTerms) {
                int df = compositeIndex.documentFrequency(searchTerm);
                List<Posting> postings = compositeIndex.getPostings(searchTerm);
                double fuzzyMultiplier = searchTerm.equals(originalTerm) ? 1.0 : 0.5;

                for(Posting posting : postings) {
                    String documentId = posting.getDocumentId();
                    if(tombstones.contains(documentId)) continue;

                    DocumentStats stats = documentStatisticsStore.get(documentId);

                    if(stats == null) continue;

                    int tf = posting.getTermFrequency();
                    int docLength = stats.length();

                    double bm25corer = bm25Scorer.score(tf, df, totalDocs, docLength, avgDocLength);
                    double boost = FieldBoosts.BOOSTS.getOrDefault(posting.getField(), 1.0);
                    double score = bm25corer * boost * fuzzyMultiplier;

                    documentExplanations
                            .computeIfAbsent(documentId, d -> new ArrayList<>())
                            .add(new ScoreExplanation(searchTerm, posting.getField().name(), bm25corer, boost, score));

                    scores.merge(documentId, score, Double::sum);
                }
            }
        }
    }

    private int indexField(String documentId, String text, FieldType field) {
        List<String> tokens = tokenizer.tokenize(text);

        for(int i=0; i < tokens.size(); i++) {
            memoryIndex.addToken(tokens.get(i), documentId, field, i);
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
                List<Posting> postings = compositeIndex.getPostings(term);

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
                if(tombstones.contains(entry.getKey())) continue;

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

    private void indexDocument(Document document) {
        int totalLength = 0;
        totalLength += indexField(document.id(), document.title(), FieldType.TITLE);
        totalLength += indexField(document.id(), document.content(), FieldType.CONTENT);
        if(document.tags() != null && !document.tags().isEmpty())
            for(String tags : document.tags())
                totalLength += indexField(document.id(), tags, FieldType.TAG);

        documentStatisticsStore.save(new DocumentStats(document.id(), totalLength));
    }

    private void flushSegment() {
        if(!flushLock.tryLock()) return;

        try {
            String segmentId = "segment_" + System.currentTimeMillis();
            segmentWriter.writeSegment(segmentId, memoryIndex.exportIndex());
            Segment segment = segmentManager.createSegment(segmentId);
            DiskSegmentIndex diskIndex = new DiskSegmentIndex(segmentReader.read(segment.getPath()));
            compositeIndex.addSegment(diskIndex);

            memoryIndex.clear();
            buffer.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            flushLock.unlock();
        }
    }

    public void deleteDocument(String documentId) {
        tombstones.add(documentId);
        documentStore.remove(documentId);
        documentStatisticsStore.remove(documentId);
    }
}
