package com.manish.search.api;

import com.manish.search.model.WeightedQuery;
import com.manish.search.search.QueryParser;
import com.manish.search.search.SearchEngine;
import com.manish.search.ranking.SearchResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SearchController {
    private final SearchEngine searchEngine;
    private final QueryParser parser;

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String q) {
        WeightedQuery query = parser.parse(q);
        return searchEngine.search(query);
    }
}
