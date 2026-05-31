package com.manish.search.storage;

import com.manish.search.model.Document;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentStore {
    private final Map<String, Document> documentMapStore;

    public DocumentStore() {
        documentMapStore = new ConcurrentHashMap<>();
    }

    public void save(Document document) {
        documentMapStore.put(document.id(), document);
    }

    public Document get(String id) {
        return documentMapStore.get(id);
    }

    public Collection<Document> getAll() {
        return documentMapStore.values();
    }

    public void remove(String documentId) {
        documentMapStore.remove(documentId);
    }
}
