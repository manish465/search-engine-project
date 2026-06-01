package com.manish.search.shard.api;

import com.manish.search.common.dto.SearchRequest;
import com.manish.search.common.dto.SearchResult;
import com.manish.search.shard.service.LocalSearchEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalSearchController {
    private final LocalSearchEngine engine;

    @PostMapping
    public List<SearchResult> search(@RequestBody SearchRequest request) {
        return engine.search(request.query());
    }
}
