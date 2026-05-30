package com.manish.search.model;

public record SearchResult(
        String documentId,
        double positiveScore,
        double negativeScore,
        double finalScore
) {}
