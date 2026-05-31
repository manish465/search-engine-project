package com.manish.search.storage;

import com.manish.search.indexing.Posting;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class SegmentWriter {
    public void writeSegment(
            String segmentId,
            Map<String, List<Posting>> index
    ) throws IOException {
        Path dir = Paths.get("segments");
        Files.createDirectories(dir);
        Path file = dir.resolve(segmentId + ".idx");

        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            for(var entry : index.entrySet()) {
                writer.write(entry.getKey());
                writer.newLine();

                for(Posting posting : entry.getValue()) {
                    writer.write(
                            posting.getDocumentId() + "|" +
                                posting.getField() + "|" +
                                posting.getTermFrequency() + "|" +
                                posting.getPositions()
                    );
                    writer.newLine();
                }

                writer.newLine();
            }

            writer.newLine();
        }
    }
}
