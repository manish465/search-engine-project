package com.manish.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotNode implements QueryNode {
    private final QueryNode child;
}
