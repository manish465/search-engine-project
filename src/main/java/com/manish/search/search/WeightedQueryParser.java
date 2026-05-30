package com.manish.search.search;

import com.manish.search.model.WeightedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeightedQueryParser {
    public WeightedQuery parse(String query) {
        List<String> positive = new ArrayList<>();
        List<String> negative = new ArrayList<>();

        String[] terms = query.split("\\s+");

        for (String term : terms) {
            if(term.startsWith("-")) {
                negative.add(term.substring(1));
            } else {
                positive.add(term.replace("+", ""));
            }
        }

        return new WeightedQuery(positive, negative);
    }
}
