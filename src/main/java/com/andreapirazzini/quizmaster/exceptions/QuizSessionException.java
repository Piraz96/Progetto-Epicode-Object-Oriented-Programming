package com.andreapirazzini.quizmaster.exceptions;

/**
 * Sollevata durante l'esecuzione di un quiz (sessione): timer interrotto,
 * input I/O fallito, scoreboard non scrivibile. Estende {@link QuizException}
 * così un singolo catch al boundary copre tutti i tipi.
 */
public class QuizSessionException extends QuizException {

    public QuizSessionException(String message) {
        super(message);
    }

    public QuizSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
