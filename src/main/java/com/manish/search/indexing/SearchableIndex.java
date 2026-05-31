package com.manish.search.indexing;

import java.util.List;
import java.util.Set;

public interface SearchableIndex {
    List<Posting> getPostings(String term);
    int documentFrequency(String term);
    Set<String> getVocabulary();
}
