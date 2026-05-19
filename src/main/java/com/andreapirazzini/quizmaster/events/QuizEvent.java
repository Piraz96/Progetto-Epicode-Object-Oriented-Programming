package com.andreapirazzini.quizmaster.events;

import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.core.QuestionResult;

/**
 * Gerarchia sigillata di eventi pubblicati sul {@link QuizEventBus}.
 * Sealed + record fa sì che i subscriber possano fare pattern matching
 * sulla variante runtime, e il compilatore avverte se aggiungiamo un
 * nuovo evento senza aggiornare gli switch.
 */
public sealed interface QuizEvent {

    /** Emesso una sola volta, prima della prima domanda. */
    record QuizStarted(String quizName, int totalQuestions) implements QuizEvent {}

    /** Emesso prima di presentare una nuova domanda all'utente. */
    record QuestionPresented(int index, int total, Question question) implements QuizEvent {}

    /** Emesso non appena l'utente fornisce una risposta (o scade il timer). */
    record AnswerGiven(Answer answer) implements QuizEvent {}

    /** Emesso dopo aver giudicato la risposta e assegnato i punti. */
    record QuestionScored(QuestionResult<?> result) implements QuizEvent {}

    /** Emesso una sola volta, alla fine del quiz. */
    record QuizCompleted(int finalScore, int totalQuestions, int correctCount) implements QuizEvent {}

    /** Tick del timer (1 al secondo). */
    record TimerTick(int secondsLeft) implements QuizEvent {}

    /** Timer scaduto senza risposta. */
    record TimerExpired() implements QuizEvent {}
}
