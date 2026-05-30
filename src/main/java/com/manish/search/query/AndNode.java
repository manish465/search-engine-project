package com.manish.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AndNode implements QueryNode {
    private final QueryNode left;
    private final QueryNode right;
}
