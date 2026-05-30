package com.manish.search.service;

import com.manish.search.indexing.InvertedIndex;
import com.manish.search.indexing.Posting;
import com.manish.search.query.*;
import com.manish.search.model.Document;
import com.manish.search.storage.DocumentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryExecutor {
    private final InvertedIndex index;
    private final DocumentStore store;

    public Set<String> execute(QueryNode node) {
        if(node instanceof TermNode termNode) return executeTerm(termNode);
        if(node instanceof AndNode andNode) return executeAnd(andNode);
        if(node instanceof OrNode orNode) return executeOr(orNode);
        if(node instanceof NotNode notNode) return executeNot(notNode);

        return Set.of();
    }

    private Set<String> executeTerm(TermNode node) {
        return index.getPostings(node.getTerm())
                .stream()
                .map(Posting::getDocumentId)
                .collect(Collectors.toSet());
    }

    private Set<String> executeAnd(AndNode node) {
        Set<String> left = execute(node.getLeft());
        Set<String> right = execute(node.getRight());
        left.retainAll(right);
        return left;
    }

    private Set<String> executeOr(OrNode node) {
        Set<String> left = execute(node.getLeft());
        Set<String> right = execute(node.getRight());
        left.addAll(right);
        return left;
    }

    private Set<String> executeNot(NotNode node) {
        Set<String> allDocuments = store.getAll()
                .stream()
                .map(Document::id)
                .collect(Collectors.toSet());

        Set<String> child = execute(node.getChild());
        allDocuments.removeAll(child);

        return allDocuments;
    }
}
