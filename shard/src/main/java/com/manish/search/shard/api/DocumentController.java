package com.manish.search.shard.api;

import com.manish.search.common.dto.Document;
import com.manish.search.shard.service.LocalSearchEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final LocalSearchEngine engine;

    @PostMapping
    public void add(@RequestBody Document doc) {
        engine.indexDocument(doc);
    }
}
