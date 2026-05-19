package com.andreapirazzini.quizmaster.core;

import java.util.Objects;

/**
 * Foglia astratta del Composite. Possiede lo stato condiviso da tutte le
 * varianti di domanda (id, testo, punti base) e delega alle sottoclassi
 * il giudizio sulla correttezza e la formattazione del prompt.
 *
 * <p>Le tre sottoclassi sono {@code MultipleChoiceQuestion},
 * {@code TrueFalseQuestion} e {@code OpenEndedQuestion}.
 */
public abstract class Question implements QuizElement {

    protected final String id;
    protected final String text;
    protected final int basePoints;

    protected Question(String id, String text, int basePoints) {
        this.id = Objects.requireNonNull(id, "id");
        this.text = Objects.requireNonNull(text, "text");
        if (basePoints < 0) {
            throw new IllegalArgumentException("basePoints non può essere negativo");
        }
        this.basePoints = basePoints;
    }

    public final String id() {
        return id;
    }

    public final String text() {
        return text;
    }

    public final int basePoints() {
        return basePoints;
    }

    @Override
    public final String title() {
        return text;
    }

    @Override
    public final int totalQuestions() {
        return 1;
    }

    /**
     * Restituisce {@code true} se la risposta utente è considerata corretta.
     * Implementazione specifica per tipo (multipla, V/F, aperta).
     */
    public abstract boolean isCorrect(String userAnswer);

    /** Testo della risposta corretta (mostrato dopo l'esito). */
    public abstract String correctAnswerText();

    /**
     * Prompt completo da mostrare all'utente: testo della domanda + eventuali
     * opzioni numerate. Una sola chiamata, il chiamante stampa così com'è.
     */
    public abstract String prompt();
}
