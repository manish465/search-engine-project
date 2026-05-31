package com.manish.search.storage;

import com.manish.search.indexing.Posting;
import com.manish.search.indexing.SearchableIndex;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class DiskSegmentIndex implements SearchableIndex {
    private final Map<String, List<Posting>> index;

    @Override
    public List<Posting> getPostings(String term) {
        return index.getOrDefault(term, List.of());
    }

    @Override
    public int documentFrequency(String term) {
        return getPostings(term).size();
    }

    @Override
    public Set<String> getVocabulary() {
        return index.keySet();
    }
}
