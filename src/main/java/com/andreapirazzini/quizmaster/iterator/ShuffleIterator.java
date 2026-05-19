package com.andreapirazzini.quizmaster.iterator;

import com.andreapirazzini.quizmaster.core.Question;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/**
 * Iterator pattern. Implementa direttamente
 * {@link java.util.Iterator} (NON {@code list.iterator()} o {@code stream()})
 * e presenta le {@link Question} in ordine casuale ma riproducibile.
 *
 * <p>Lo shuffle è Fisher-Yates con {@link Random} seedabile: passando lo
 * stesso seed si ottiene la stessa permutazione (utile per i test e per
 * "rigiocare la stessa partita").
 *
 * <p>Una volta esaurito, {@link #next()} solleva {@link NoSuchElementException}
 * come prescrive il contratto JDK.
 */
public final class ShuffleIterator implements Iterator<Question> {

    private final Question[] shuffled;
    private int next;

    /** Costruttore principale: shuffle deterministico col seed dato. */
    public ShuffleIterator(List<Question> questions, long seed) {
        Objects.requireNonNull(questions, "questions");
        this.shuffled = questions.toArray(Question[]::new);
        Random rng = new Random(seed);
        // Fisher-Yates classico, in-place.
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Question tmp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = tmp;
        }
        this.next = 0;
    }

    /** Shortcut con seed basato sul nanotime (non-deterministico). */
    public ShuffleIterator(List<Question> questions) {
        this(questions, System.nanoTime());
    }

    @Override
    public boolean hasNext() {
        return next < shuffled.length;
    }

    @Override
    public Question next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Iterazione esaurita.");
        }
        return shuffled[next++];
    }

    /** Numero di domande totali (utile per il display "domanda 3 di 10"). */
    public int totalCount() {
        return shuffled.length;
    }

    /** Numero di domande ancora da emettere. */
    public int remaining() {
        return shuffled.length - next;
    }

    /** Numero di domande già emesse. */
    public int emittedCount() {
        return next;
    }
}
