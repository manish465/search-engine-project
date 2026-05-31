package com.manish.search.search;

public record ScoreExplanation(
        String term,
        String field,
        double bm25,
        double boost,
        double contribution
) {
}
