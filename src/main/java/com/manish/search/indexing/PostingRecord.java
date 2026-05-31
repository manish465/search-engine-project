package com.manish.search.indexing;

import java.util.List;

public record PostingRecord(
        String documentId,
        String field,
        int frequency,
        List<Integer> positions
) {
}
