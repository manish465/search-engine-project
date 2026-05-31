package com.manish.search.indexing;

import com.manish.search.model.FieldType;

public record PostingKey(String documentId, FieldType field) {
}
