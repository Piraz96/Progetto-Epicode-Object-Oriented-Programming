package com.andreapirazzini.quizmaster.session;

import java.time.Instant;
import java.util.Objects;

/**
 * Risultato di una sessione di quiz completata. Immutable record, serializzato
 * nel {@code scoreboard.json} e mostrato nel riepilogo finale.
 */
public record SessionResult(
        String quizName,
        int finalScore,
        int correctCount,
        int totalQuestions,
        Instant playedAt
) {
    public SessionResult {
        Objects.requireNonNull(quizName, "quizName");
        Objects.requireNonNull(playedAt, "playedAt");
    }

    /** Percentuale di risposte corrette (0–100). */
    public int percentage() {
        if (totalQuestions == 0) return 0;
        return (int) Math.round(100.0 * correctCount / totalQuestions);
    }
}
