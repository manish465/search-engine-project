package com.manish.search.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class DocumentStats {
    private final String documentId;
    private final int length;
}
