package com.manish.search.shard.storage;

import com.manish.search.common.dto.Document;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentStore {
    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    public void save(Document doc) {
        documents.put(doc.id(), doc);
    }

    public Document get(String id) {
        return documents.get(id);
    }

    public int size() {
        return documents.size();
    }
}
