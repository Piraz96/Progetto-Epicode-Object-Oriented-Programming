package com.andreapirazzini.quizmaster.events.listeners;

import com.andreapirazzini.quizmaster.cli.AnsiColor;
import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventListener;

/**
 * Reagisce agli eventi del quiz stampando "feedback live" colorato su
 * {@code System.out}: ✓ verde se corretto, ✗ rosso se sbagliato, timer
 * ⏱ giallo sulla stessa riga via carriage return.
 *
 * <p>Una delle pochissime classi autorizzate a usare {@code System.out}
 * (le altre sono {@code QuizMasterApp.main} e {@code ConsoleUI}).
 */
public final class ConsoleAnimationListener implements QuizEventListener {

    @Override
    public void onEvent(QuizEvent event) {
        switch (event) {
            case QuizEvent.QuizStarted s ->
                    System.out.println(AnsiColor.bold(AnsiColor.cyan(
                            "\n>>> Inizio quiz: " + s.quizName() + " (" + s.totalQuestions() + " domande)\n")));

            case QuizEvent.QuestionPresented qp -> {
                System.out.println(AnsiColor.bold(
                        "\nDomanda " + qp.index() + "/" + qp.total()
                                + AnsiColor.dim(" (" + qp.question().basePoints() + " pt)")));
                // Stampa subito il prompt della domanda; l'utente vedrà
                // testo + eventuali opzioni + ": " per la risposta.
                System.out.print(qp.question().prompt());
                System.out.flush();
            }

            case QuizEvent.QuestionScored qs -> {
                if (qs.result().correct()) {
                    System.out.println(AnsiColor.green(
                            "✓ Corretto! +" + qs.result().pointsAwarded() + " punti"));
                } else {
                    System.out.println(AnsiColor.red("✗ Sbagliato."));
                }
            }

            case QuizEvent.TimerTick ignored -> {
                // Volutamente NO live updates: in CLI bloccante interferirebbero col
                // prompt dell'utente. Il TimerExpired sotto basta per la "scena".
            }

            case QuizEvent.TimerExpired e ->
                    System.out.println("\n" + AnsiColor.red("⏰ Tempo scaduto!"));

            case QuizEvent.QuizCompleted qc ->
                    System.out.println(AnsiColor.bold(AnsiColor.magenta(
                            "\n=== Quiz completato: " + qc.correctCount() + "/" + qc.totalQuestions()
                                    + " corrette, punteggio " + qc.finalScore() + " ===\n")));

            case QuizEvent.AnswerGiven ignored -> {
                // Niente da stampare — il QuestionScored seguirà subito dopo.
            }
        }
    }
}
