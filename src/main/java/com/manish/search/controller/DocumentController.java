package com.manish.search.controller;

import com.manish.search.search.SearchEngine;
import com.manish.search.storage.Document;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {
    private final SearchEngine searchEngine;

    @PostMapping
    public String addDocument(@RequestBody Document document) {
        searchEngine.index(document);
        return "Indexed";
    }
}
