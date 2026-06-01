package com.manish.search.coordinator.registry;

import com.manish.search.coordinator.model.ShardNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShardRegistry {
    public List<ShardNode> shards() {
        return List.of(
                new ShardNode("shard1", 8080),
                new ShardNode("shard2", 8080),
                new ShardNode("shard3", 8080)
        );
    }
}
