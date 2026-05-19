package com.andreapirazzini.quizmaster.core;

/**
 * Esito di una singola domanda. Generic record per portare un payload
 * opzionale specifico al tipo di domanda (es. la lettera scelta per le
 * multiple choice, la stringa data per le aperte).
 *
 * @param questionId   id della domanda
 * @param correct      esito booleano
 * @param pointsAwarded punti effettivamente assegnati dalla strategia
 * @param detail        payload opzionale tipato
 * @param <T>          tipo del payload
 */
public record QuestionResult<T>(String questionId, boolean correct, int pointsAwarded, T detail) {

    public static <T> QuestionResult<T> correct(String id, int points, T detail) {
        return new QuestionResult<>(id, true, points, detail);
    }

    public static <T> QuestionResult<T> wrong(String id, T detail) {
        return new QuestionResult<>(id, false, 0, detail);
    }
}
