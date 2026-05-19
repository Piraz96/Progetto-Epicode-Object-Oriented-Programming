package com.andreapirazzini.quizmaster.events.listeners;

import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Accumula statistiche del quiz in corso (corrette, sbagliate, punti totali).
 * Usato dal {@code QuizSession} per generare il riepilogo finale e dal
 * leaderboard per salvare il punteggio.
 *
 * <p>Tutti i contatori sono atomici perché aggiornati anche da thread del
 * timer di background.
 */
public final class StatsCollectorListener implements QuizEventListener {

    private final AtomicInteger correctCount = new AtomicInteger(0);
    private final AtomicInteger wrongCount = new AtomicInteger(0);
    private final AtomicInteger totalScore = new AtomicInteger(0);

    @Override
    public void onEvent(QuizEvent event) {
        if (event instanceof QuizEvent.QuestionScored qs) {
            if (qs.result().correct()) {
                correctCount.incrementAndGet();
                totalScore.addAndGet(qs.result().pointsAwarded());
            } else {
                wrongCount.incrementAndGet();
            }
        }
    }

    public int correctCount() { return correctCount.get(); }
    public int wrongCount()   { return wrongCount.get(); }
    public int totalScore()   { return totalScore.get(); }
}
