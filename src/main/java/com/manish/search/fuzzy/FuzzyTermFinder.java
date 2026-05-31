package com.manish.search.fuzzy;

import com.manish.search.indexing.InvertedIndex;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FuzzyTermFinder {
    private final InvertedIndex index;
    private final LevenshteinDistanceCalculator calculator;

    public List<String> findSimilarTerms(String queryTerm) {
        List<String> result = new ArrayList<>();

        for(String term : index.getVocabulary()) {
            int distance = calculator.calculate(queryTerm, term);
            if(distance <= 1) result.add(term);
        }

        return result;
    }
}
