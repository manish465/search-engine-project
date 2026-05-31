package com.manish.search.query;

import java.util.List;

public record PhraseQuery(List<String> terms) implements Query {
}
