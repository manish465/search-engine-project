package com.manish.search.shard.service;

import com.manish.search.common.dto.SearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalSearchEngine {
    public List<SearchResult> search(String query) {
        return List.of(new SearchResult("doc-" + query.hashCode(), Math.random() * 10));
    }
}
