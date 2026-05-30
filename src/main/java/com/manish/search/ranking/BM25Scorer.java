package com.manish.search.ranking;

import org.springframework.stereotype.Component;

@Component
public class BM25Scorer {
    private static final double K1 = 1.5;
    private static final double B = 0.75;

    public double score(int tf, int df, int totalDocs, int docLength, double avgDocLength) {
        double idf = Math.log(1 + ((totalDocs - df + 0.5) / (df + 0.5)));
        double numerator = tf * (K1 + 1);
        double denominator = tf + K1 * (1 - B + B * (docLength / avgDocLength));

        return idf * (numerator / denominator);
    }
}
