package com.manish.search.indexing;

import java.util.Arrays;
import java.util.List;

public class Tokenizer {
    public List<String> tokenize(String text) {
        return Arrays.stream(
            text
                .toLowerCase()
                .split("\\W+")
        ).toList();
    }
}
