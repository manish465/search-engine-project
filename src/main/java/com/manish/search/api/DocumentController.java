package com.manish.search.api;

import com.manish.search.search.SearchEngine;
import com.manish.search.model.Document;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/all")
    public String addAllDocument(@RequestBody List<Document> documents) {
        for(Document document : documents) {
            searchEngine.index(document);
        }
        return "Indexed All";
    }

    @DeleteMapping
    public String deleteDocument(@RequestParam String id) {
        searchEngine.deleteDocument(id);
        return "Deleted Document";
    }
}
