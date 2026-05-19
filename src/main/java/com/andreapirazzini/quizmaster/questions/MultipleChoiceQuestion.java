package com.andreapirazzini.quizmaster.questions;

import com.andreapirazzini.quizmaster.core.Question;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Domanda a scelta multipla con 2-6 opzioni etichettate A, B, C…
 * L'utente risponde digitando la lettera (case-insensitive) oppure
 * il numero corrispondente (1, 2, 3…).
 */
public final class MultipleChoiceQuestion extends Question {

    private static final String LETTERS = "ABCDEF";

    private final List<String> options;
    private final int correctIndex;

    public MultipleChoiceQuestion(String id, String text, List<String> options,
                                  int correctIndex, int basePoints) {
        super(id, text, basePoints);
        Objects.requireNonNull(options, "options");
        if (options.size() < 2 || options.size() > LETTERS.length()) {
            throw new IllegalArgumentException(
                    "MultipleChoice deve avere 2-" + LETTERS.length() + " opzioni, ricevute " + options.size());
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException(
                    "correctIndex fuori range: " + correctIndex);
        }
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
    }

    public List<String> options() {
        return options;
    }

    public int correctIndex() {
        return correctIndex;
    }

    @Override
    public boolean isCorrect(String userAnswer) {
        if (userAnswer == null) {
            return false;
        }
        String normalized = userAnswer.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }
        // Lettera (A, B, C…)
        if (normalized.length() == 1) {
            int idx = LETTERS.indexOf(normalized.charAt(0));
            if (idx >= 0) {
                return idx == correctIndex;
            }
        }
        // Numero (1, 2, 3…)
        try {
            int n = Integer.parseInt(normalized);
            return (n - 1) == correctIndex;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public String correctAnswerText() {
        return LETTERS.charAt(correctIndex) + ") " + options.get(correctIndex);
    }

    @Override
    public String prompt() {
        StringBuilder sb = new StringBuilder(text).append('\n');
        for (int i = 0; i < options.size(); i++) {
            sb.append("  ").append(LETTERS.charAt(i)).append(") ")
                    .append(options.get(i)).append('\n');
        }
        sb.append("Risposta (lettera o numero): ");
        return sb.toString();
    }
}
