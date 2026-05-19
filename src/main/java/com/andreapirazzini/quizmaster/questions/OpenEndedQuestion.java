package com.andreapirazzini.quizmaster.questions;

import com.andreapirazzini.quizmaster.core.Question;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Domanda a risposta aperta. Accetta più varianti come "risposta corretta"
 * (sinonimi, forme alternative). Il match è case-insensitive, ignora
 * spazi extra e ignora accenti (così "perché" matcha "perche").
 */
public final class OpenEndedQuestion extends Question {

    private final List<String> acceptedAnswers;

    public OpenEndedQuestion(String id, String text, List<String> acceptedAnswers, int basePoints) {
        super(id, text, basePoints);
        Objects.requireNonNull(acceptedAnswers, "acceptedAnswers");
        if (acceptedAnswers.isEmpty()) {
            throw new IllegalArgumentException("OpenEnded richiede almeno una risposta accettata");
        }
        this.acceptedAnswers = List.copyOf(acceptedAnswers);
    }

    public List<String> acceptedAnswers() {
        return acceptedAnswers;
    }

    @Override
    public boolean isCorrect(String userAnswer) {
        if (userAnswer == null) return false;
        String normalized = normalize(userAnswer);
        if (normalized.isEmpty()) return false;
        return acceptedAnswers.stream()
                .map(OpenEndedQuestion::normalize)
                .anyMatch(normalized::equals);
    }

    @Override
    public String correctAnswerText() {
        // Mostra solo la prima — quella "canonica" per il quiz.
        return acceptedAnswers.get(0);
    }

    @Override
    public String prompt() {
        return text + "\nRisposta libera: ";
    }

    /** Lowercase + trim + strip accenti per match tollerante. */
    private static String normalize(String s) {
        String stripped = Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD);
        return stripped.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
