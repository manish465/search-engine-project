package com.manish.search.indexing;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Tokenizer {
    public List<String> tokenize(String text) {
        if(text == null || text.isBlank()) return List.of();

        return Arrays.stream(
            text.toLowerCase().split("\\W+")
        ).filter(token -> !token.isBlank()).toList();
    }
}
