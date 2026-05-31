package com.manish.search.indexing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Posting {
    private final String documentId;
    private final FieldType field;
    private int termFrequency;
    private final List<Integer> positions;

    public Posting(String documentId, FieldType field) {
        this.field = field;
        this.documentId = documentId;
        positions = new ArrayList<>();
    }

    public void addPosition(int position) {
        positions.add(position);
        termFrequency++;
    }
}
