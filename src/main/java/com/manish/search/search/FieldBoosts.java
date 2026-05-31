package com.manish.search.search;

import com.manish.search.model.FieldType;

import java.util.Map;

public class FieldBoosts {
    public static final Map<FieldType, Double> BOOSTS = Map.of(
        FieldType.TITLE, 3.0,
        FieldType.CONTENT, 1.0,
        FieldType.TAG, 5.0
    );
}
