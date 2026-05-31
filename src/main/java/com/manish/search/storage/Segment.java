package com.manish.search.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
public class Segment {
    private final String id;
    private final Path path;
}
