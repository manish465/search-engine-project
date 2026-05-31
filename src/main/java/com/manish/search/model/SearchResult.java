package com.manish.search.model;

import com.manish.search.search.ScoreExplanation;

import java.util.List;

public record SearchResult(
        String documentId,
        double positiveScore,
        double negativeScore,
        double finalScore,
        List<ScoreExplanation> explanations
) {}
