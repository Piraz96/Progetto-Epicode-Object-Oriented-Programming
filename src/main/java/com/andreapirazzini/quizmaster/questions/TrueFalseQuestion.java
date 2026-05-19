package com.andreapirazzini.quizmaster.questions;

import com.andreapirazzini.quizmaster.core.Question;

import java.util.Locale;
import java.util.Set;

/**
 * Domanda Vero/Falso. Accetta come input: V/F, vero/falso, true/false,
 * T/F, sì/no, s/n — tutto case-insensitive.
 */
public final class TrueFalseQuestion extends Question {

    private static final Set<String> TRUE_TOKENS =
            Set.of("V", "VERO", "TRUE", "T", "SI", "SÌ", "S", "Y", "YES");
    private static final Set<String> FALSE_TOKENS =
            Set.of("F", "FALSO", "FALSE", "NO", "N");

    private final boolean correctAnswer;

    public TrueFalseQuestion(String id, String text, boolean correctAnswer, int basePoints) {
        super(id, text, basePoints);
        this.correctAnswer = correctAnswer;
    }

    public boolean correctAnswer() {
        return correctAnswer;
    }

    @Override
    public boolean isCorrect(String userAnswer) {
        if (userAnswer == null) return false;
        String token = userAnswer.trim().toUpperCase(Locale.ROOT);
        if (TRUE_TOKENS.contains(token))  return correctAnswer;
        if (FALSE_TOKENS.contains(token)) return !correctAnswer;
        return false;
    }

    @Override
    public String correctAnswerText() {
        return correctAnswer ? "Vero" : "Falso";
    }

    @Override
    public String prompt() {
        return text + "\nRisposta (V/F): ";
    }
}
