package io.cockroachdb.test.unpack;

import java.io.IOException;
import java.nio.file.Path;

public interface Extractor {
    boolean supports(ArchiveType archiveType);

    void extractTo(Path archive, String mimeType, Path destination) throws IOException;
}
