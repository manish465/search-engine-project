package com.manish.search.indexing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Posting {
    private final String documentId;
    private int termFrequency;
    private final List<Integer> positions;

    public Posting(String documentId) {
        this.documentId = documentId;
        positions = new ArrayList<>();
    }

    public void addPosition(int position) {
        positions.add(position);
        termFrequency++;
    }
}
