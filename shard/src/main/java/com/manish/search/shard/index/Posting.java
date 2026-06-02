package com.manish.search.shard.index;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Posting {
    private final String documentId;
    private int frequency = 0;
    private final List<Integer> positions = new ArrayList<>();

    public void addPosition(int position) {
        positions.add(position);
        frequency++;
    }
}
