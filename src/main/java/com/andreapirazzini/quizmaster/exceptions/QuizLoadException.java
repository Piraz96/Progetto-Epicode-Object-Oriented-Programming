package com.andreapirazzini.quizmaster.exceptions;

/**
 * Sollevata quando un file quiz JSON è malformato: campo mancante, type
 * sconosciuto, JSON corrotto. Il messaggio è già pulito e si mostra
 * verbatim all'utente.
 */
public class QuizLoadException extends QuizException {

    public QuizLoadException(String message) {
        super(message);
    }

    public QuizLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
