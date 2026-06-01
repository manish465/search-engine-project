package com.manish.search.coordinator.api;

import com.manish.search.common.dto.SearchResult;
import com.manish.search.coordinator.service.SearchCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchCoordinator coordinator;

    @GetMapping
    public List<SearchResult> search(@RequestParam String q) throws Exception {
        return coordinator.search(q);
    }
}
