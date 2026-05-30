package com.manish.search.controller;

import com.manish.search.service.SearchEngine;
import com.manish.search.search.SearchResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SearchController {
    private final SearchEngine searchEngine;

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String q) {
        return searchEngine.search(q);
    }
}
