package com.andreapirazzini.quizmaster.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Scrittura UTF-8 con {@link BufferedWriter} in try-with-resources.
 * Crea le directory parent al volo.
 */
public final class SafeFileWriter {

    private SafeFileWriter() {
        // utility
    }

    public static void writeString(Path path, String content) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }
}
