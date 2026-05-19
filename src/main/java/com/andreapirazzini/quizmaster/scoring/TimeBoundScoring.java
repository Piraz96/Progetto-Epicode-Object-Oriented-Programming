package com.andreapirazzini.quizmaster.scoring;

import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;

/**
 * Punteggio "veloce paga": se la risposta è corretta, i punti sono
 * {@code basePoints + secondsLeft}. Risposta sbagliata o timeout → 0.
 *
 * <p>Esempio: con timer 15s, rispondendo dopo 5s → 1 + 10 = 11 punti.
 * Rispondendo all'ultimo secondo → 1 + 0 = 1 punto. Premia la velocità.
 */
public final class TimeBoundScoring implements ScoringStrategy {

    @Override
    public int score(Question question, Answer answer, int secondsLeft, int durationSeconds) {
        if (answer.isTimeout() || !question.isCorrect(answer.value())) {
            return 0;
        }
        return question.basePoints() + Math.max(0, secondsLeft);
    }

    @Override
    public String name() {
        return "time";
    }

    @Override
    public String description() {
        return "A tempo — punti = base + secondi rimasti. Premia la velocità.";
    }
}
