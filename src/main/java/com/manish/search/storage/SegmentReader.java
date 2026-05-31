package com.manish.search.storage;

import com.manish.search.indexing.FieldType;
import com.manish.search.indexing.Posting;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SegmentReader {
    public Map<String, List<Posting>> read(Path path) throws IOException {
        Map<String, List<Posting>> index = new HashMap<>();
        List<String> lines = Files.readAllLines(path);

        String currentTerm = null;

        for(String line : lines) {
            line = line.trim();

            if(line.isBlank()) {
                currentTerm = null;
                continue;
            }

            if(!line.contains("|")) {
                currentTerm = line;
                index.putIfAbsent(currentTerm, new ArrayList<>());
                continue;
            }

            String[] parts = line.split("\\|");
            String documentId = parts[0];
            FieldType field = FieldType.valueOf(parts[1]);
            String positionsRaw = parts[3].replace("[", "").replace("]", "");
            Posting posting = new Posting(documentId, field);

            if(!positionsRaw.isBlank()) {
                for (String p : positionsRaw.split(",")) {
                    posting.addPosition(Integer.parseInt(p.trim()));
                }
            }

            index.get(currentTerm).add(posting);
        }

        return index;
    }
}
