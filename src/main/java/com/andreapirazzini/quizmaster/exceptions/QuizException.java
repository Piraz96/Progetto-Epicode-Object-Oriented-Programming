package com.andreapirazzini.quizmaster.exceptions;

/**
 * Radice della gerarchia pubblica delle eccezioni. Qualunque cosa esca da
 * {@code QuizMasterApp.main} verso l'utente deve essere una {@code QuizException}
 * (o sottoclasse) — le cause interne (I/O, runtime) sono wrappate da
 * {@link com.andreapirazzini.quizmaster.shielding.ExceptionShield} prima di arrivare qui.
 *
 * <p>I messaggi sono sanitizzati lato chiamante: chi costruisce una QuizException
 * deve mettere solo informazioni mostrabili all'utente. Stack trace e cause
 * originali finiscono nel log file, mai in {@code getMessage()}.
 */
public class QuizException extends RuntimeException {

    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}
