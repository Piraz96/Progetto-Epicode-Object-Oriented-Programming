package com.andreapirazzini.quizmaster.core;

import java.util.List;
import java.util.Objects;

/**
 * Composito di livello medio: una collezione ordinata di {@link Question}.
 * Tipicamente caricato da un singolo file JSON (es. {@code storia.json}).
 *
 * <p>Immutabile: le question vengono passate al costruttore, copiate
 * difensivamente, e non sono più modificabili dall'esterno.
 */
public final class Quiz implements QuizElement {

    private final String name;
    private final String description;
    private final List<Question> questions;

    public Quiz(String name, String description, List<Question> questions) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNullElse(description, "");
        this.questions = List.copyOf(Objects.requireNonNull(questions, "questions"));
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    /** Vista read-only delle domande. */
    public List<Question> questions() {
        return questions;
    }

    @Override
    public String title() {
        return name;
    }

    @Override
    public int totalQuestions() {
        return questions.size();
    }

    @Override
    public boolean isComposite() {
        return true;
    }
}
