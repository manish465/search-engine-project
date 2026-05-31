package com.manish.search.storage;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class SegmentManager {
    @Getter
    private final List<Segment> segments;

    public SegmentManager() {
        this.segments = new ArrayList<>();
    }

    public Segment createSegment(String id){
        Segment segment = new Segment(
                id, Paths.get("segments", id + ".idx")
        );
        segments.add(segment);
        return segment;
    }
}
