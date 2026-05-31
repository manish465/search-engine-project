package com.manish.search.ranking;

import java.util.List;

public record SearchResult(
        String documentId,
        double positiveScore,
        double negativeScore,
        double finalScore,
        List<ScoreExplanation> explanations
) {}
