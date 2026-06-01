package com.manish.search.coordinator.registry;

import com.manish.search.coordinator.model.ShardNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShardRegistry {
    public List<ShardNode> shards() {
        return List.of(
                new ShardNode("localhost", 9001),
                new ShardNode("localhost", 9002),
                new ShardNode("localhost", 9003)
        );
    }
}
