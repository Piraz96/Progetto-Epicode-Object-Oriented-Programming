package com.andreapirazzini.quizmaster.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lettura UTF-8 di file via {@code java.nio.file.Files}. Centralizza la
 * scelta del charset e tiene {@code throws IOException} fuori dal codice
 * chiamante. Uso try-with-resources (interno a {@code Files.readString}).
 */
public final class SafeFileReader {

    private SafeFileReader() {
        // utility
    }

    /** Legge un intero file in una singola stringa UTF-8. */
    public static String readString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
