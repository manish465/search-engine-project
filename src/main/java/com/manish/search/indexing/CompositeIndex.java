package com.manish.search.indexing;

import com.manish.search.storage.DiskSegmentIndex;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CompositeIndex implements SearchableIndex {
    private final MemoryIndex memoryIndex;
    private final List<DiskSegmentIndex> segments = new ArrayList<>();

    public void addSegment(DiskSegmentIndex segment) {
        segments.add(segment);
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> result = new ArrayList<>(memoryIndex.getPostings(term));

        for(var segment : segments){
            result.addAll(segment.getPostings(term));
        }

        return result;
    }

    @Override
    public int documentFrequency(String term) {
        return getPostings(term).size();
    }

    @Override
    public Set<String> getVocabulary() {
        Set<String> vocabulary = new HashSet<>(memoryIndex.getVocabulary());

        for(var segment : segments) {
            vocabulary.addAll(segment.getVocabulary());
        }

        return vocabulary;
    }
}
