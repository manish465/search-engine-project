package com.manish.search.model;

import java.util.List;

public record Document(
        String id,
        String title,
        String content,
        List<String> tags
) {
}
