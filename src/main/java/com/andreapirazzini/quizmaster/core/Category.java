package com.andreapirazzini.quizmaster.core;

import java.util.List;
import java.util.Objects;

/**
 * Composito di livello top: una categoria (es. "Storia", "Cinema") che
 * raggruppa più {@link Quiz}. Permette di mostrare nel menu un livello
 * gerarchico in più rispetto al singolo quiz.
 *
 * <p>Dimostra la <b>vera</b> ricorsione del Composite pattern: Category
 * contiene Quiz, Quiz contiene Question — tre livelli, non uno solo.
 */
public final class Category implements QuizElement {

    private final String name;
    private final List<Quiz> quizzes;

    public Category(String name, List<Quiz> quizzes) {
        this.name = Objects.requireNonNull(name, "name");
        this.quizzes = List.copyOf(Objects.requireNonNull(quizzes, "quizzes"));
    }

    public String name() {
        return name;
    }

    /** Vista read-only dei quiz contenuti nella categoria. */
    public List<Quiz> quizzes() {
        return quizzes;
    }

    @Override
    public String title() {
        return name;
    }

    @Override
    public int totalQuestions() {
        // Somma ricorsiva attraverso i quiz figli.
        return quizzes.stream().mapToInt(QuizElement::totalQuestions).sum();
    }

    @Override
    public boolean isComposite() {
        return true;
    }
}
