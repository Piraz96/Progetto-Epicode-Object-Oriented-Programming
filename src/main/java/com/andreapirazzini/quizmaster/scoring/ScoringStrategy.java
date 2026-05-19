package com.andreapirazzini.quizmaster.scoring;

import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;

/**
 * Strategy pattern. Calcola i punti per una singola
 * risposta. La scelta della strategia avviene a runtime (l'utente la sceglie
 * dal menu prima di iniziare il quiz).
 *
 * <p>Due implementazioni concrete:
 * <ul>
 *   <li>{@link ClassicScoring}: punteggio binario (corretto = basePoints, sbagliato = 0).</li>
 *   <li>{@link TimeBoundScoring}: bonus proporzionale ai secondi rimasti.</li>
 * </ul>
 */
public interface ScoringStrategy {

    /**
     * Calcola i punti per la risposta data.
     *
     * @param question       domanda corrente
     * @param answer         risposta utente
     * @param secondsLeft    secondi che restavano sul timer al momento della risposta
     * @param durationSeconds durata totale del timer (per normalizzare se serve)
     * @return punti da assegnare (sempre &ge; 0)
     */
    int score(Question question, Answer answer, int secondsLeft, int durationSeconds);

    /** Identificatore breve (es. "classic", "time"). */
    String name();

    /** Descrizione human-readable mostrata nel menu di scelta. */
    String description();
}
