package com.manish.search.search;

public record SearchResult(
        String documentId,
        double score
) {}
