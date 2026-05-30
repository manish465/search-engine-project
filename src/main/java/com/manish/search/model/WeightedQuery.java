package com.manish.search.model;

import java.util.List;

public record WeightedQuery(
        List<String> positiveTerms,
        List<String> negativeTerms
) {
}
