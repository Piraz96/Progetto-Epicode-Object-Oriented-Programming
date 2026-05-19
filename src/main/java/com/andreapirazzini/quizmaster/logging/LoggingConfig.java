package com.andreapirazzini.quizmaster.logging;

import com.andreapirazzini.quizmaster.exceptions.QuizException;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Carica {@code /logging.properties} dentro {@link LogManager} al boot.
 * Due handler configurati nel properties:
 * <ul>
 *   <li>{@code ConsoleHandler} a INFO — terse, user-facing.</li>
 *   <li>{@code FileHandler} a FINE — dettaglio completo, rotazione 1 MiB × 3.</li>
 * </ul>
 *
 * <p>Da chiamare una sola volta da {@code QuizMasterApp.main} prima di qualsiasi
 * altro codice che chieda un logger.
 */
public final class LoggingConfig {

    private LoggingConfig() {
        // utility
    }

    public static void init() {
        try (InputStream in = LoggingConfig.class.getResourceAsStream("/logging.properties")) {
            if (in == null) {
                return;  // niente file → JUL usa i default
            }
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            throw new QuizException("Impossibile caricare la configurazione di logging.", e);
        }
    }
}
