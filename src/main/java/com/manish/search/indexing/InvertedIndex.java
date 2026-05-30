package com.manish.search.indexing;

import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class InvertedIndex {
    private final Map<String, Set<String>> index;

    public InvertedIndex() {
        index = new ConcurrentHashMap<>();
    }
}
