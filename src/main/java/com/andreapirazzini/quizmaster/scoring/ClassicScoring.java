package com.andreapirazzini.quizmaster.scoring;

import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;

/**
 * Punteggio classico: risposta corretta → {@code question.basePoints()},
 * altrimenti 0. Il tempo viene ignorato.
 */
public final class ClassicScoring implements ScoringStrategy {

    @Override
    public int score(Question question, Answer answer, int secondsLeft, int durationSeconds) {
        return question.isCorrect(answer.value()) ? question.basePoints() : 0;
    }

    @Override
    public String name() {
        return "classic";
    }

    @Override
    public String description() {
        return "Classico — 1 punto (o basePoints) per risposta corretta, niente bonus tempo.";
    }
}
