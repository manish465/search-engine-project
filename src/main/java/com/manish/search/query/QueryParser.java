package com.manish.search.query;

import org.springframework.stereotype.Component;

@Component
public class QueryParser {
    public QueryNode parse(String query) {
        String[] parts = query.trim().split("\\s+");

        if(parts.length == 1) return new TermNode(parts[0]);
        if(parts.length == 3) {
            String left = parts[0];
            String operator = parts[1];
            String right = parts[2];

            return switch (operator.toUpperCase()) {
                case "AND" -> new AndNode(new TermNode(left), new TermNode(right));
                case "OR" -> new OrNode(new TermNode(left), new TermNode(right));
                case "NOT" -> new AndNode(new TermNode(left), new NotNode(new TermNode(right)));
                default -> throw new IllegalArgumentException("Unknown operator");
            };
        }

        throw new IllegalArgumentException(
                "Invalid query"
        );
    }
}
