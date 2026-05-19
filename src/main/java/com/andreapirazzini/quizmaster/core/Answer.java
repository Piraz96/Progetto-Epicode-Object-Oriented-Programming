package com.andreapirazzini.quizmaster.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Risposta data dall'utente a una singola domanda, insieme al tempo
 * impiegato per fornirla. Immutable record — viene passata alla
 * {@link com.andreapirazzini.quizmaster.scoring.ScoringStrategy} per calcolare i punti.
 *
 * @param value         testo della risposta utente, già sanitizzato a monte
 * @param timeTaken     durata trascorsa tra prompt e risposta
 */
public record Answer(String value, Duration timeTaken) {
    public Answer {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(timeTaken, "timeTaken");
    }

    /** Sentinel per indicare timeout senza risposta. */
    public static Answer timeout(Duration elapsed) {
        return new Answer("", elapsed);
    }

    public boolean isTimeout() {
        return value.isBlank();
    }
}
