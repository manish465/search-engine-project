package com.manish.search.coordinator.service;

import com.manish.search.common.dto.SearchRequest;
import com.manish.search.common.dto.SearchResult;
import com.manish.search.coordinator.model.ShardNode;
import com.manish.search.coordinator.registry.ShardRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class SearchCoordinator {
    private final ShardRegistry registry;

    public List<SearchResult> search(String query) throws Exception {
        List<SearchResult> allResults = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<SearchResult>>> futures = new ArrayList<>();

            for(ShardNode shard : registry.shards()) {
                futures.add(executor.submit(() -> queryShard(shard, query)));
            }

            for(Future<List<SearchResult>> future : futures) {
                allResults.addAll(future.get());
            }
        }

        return allResults.stream()
                .sorted(Comparator.comparingDouble(
                        SearchResult::score
                ).reversed())
                .toList();
    }

    private List<SearchResult> queryShard(ShardNode shard, String query) {
        RestClient client = RestClient.create();

        return client.post()
                .uri("http://%s:%d/internal/search".formatted(shard.host(), shard.port()))
                .body(new SearchRequest(query))
                .retrieve()
                .body(new ParameterizedTypeReference<List<SearchResult>>() {});
    }
}
