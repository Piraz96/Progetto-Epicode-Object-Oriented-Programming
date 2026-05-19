package com.andreapirazzini.quizmaster.events;

/**
 * Subscriber sul {@link QuizEventBus}. Functional interface, così i test
 * possono iscrivere semplici lambda.
 */
@FunctionalInterface
public interface QuizEventListener {
    void onEvent(QuizEvent event);
}
