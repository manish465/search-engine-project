package com.manish.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TermNode implements QueryNode {
    private final String term;
}
