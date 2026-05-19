package com.andreapirazzini.quizmaster.shielding;

import com.andreapirazzini.quizmaster.exceptions.QuizException;
import com.andreapirazzini.quizmaster.exceptions.QuizLoadException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionShieldTest {

    /** Path "sensibile" che non deve mai uscire nel messaggio user-facing. */
    private static final String INTERNAL_PATH = "/Users/segreto/.aws/credentials";

    @Test
    void passaIlValoreInHappyPath() {
        String result = ExceptionShield.run("saluto", () -> "ciao");
        assertEquals("ciao", result);
    }

    @Test
    void wrappaIoExceptionConMessaggioGenerico() {
        QuizException thrown = assertThrows(QuizException.class,
                () -> ExceptionShield.run("lettura file", () -> {
                    throw new IOException("permission denied su " + INTERNAL_PATH);
                }));

        assertNotNull(thrown.getMessage());
        assertEquals("Errore di I/O durante lettura file. Vedi il log per dettagli.",
                thrown.getMessage());
        assertFalse(thrown.getMessage().contains(INTERNAL_PATH),
                "il path sensibile non deve apparire nel messaggio user-facing");
        assertFalse(thrown.getMessage().toLowerCase().contains("permission denied"),
                "il messaggio OS originale non deve apparire");
    }

    @Test
    void wrappaRuntimeExceptionConMessaggioGenerico() {
        QuizException thrown = assertThrows(QuizException.class,
                () -> ExceptionShield.run("calcolo", () -> {
                    throw new IllegalStateException("stato corrotto offset 0xdeadbeef");
                }));

        assertEquals("Errore interno durante calcolo. Vedi il log per dettagli.",
                thrown.getMessage());
        assertFalse(thrown.getMessage().contains("0xdeadbeef"));
    }

    @Test
    void rilanciaQuizExceptionSenzaWrap() {
        QuizLoadException original = new QuizLoadException("file vuoto");

        QuizException thrown = assertThrows(QuizException.class,
                () -> ExceptionShield.run("parse", () -> { throw original; }));

        assertSame(original, thrown,
                "le QuizException già sanitizzate passano inalterate");
        assertEquals("file vuoto", thrown.getMessage());
    }

    @Test
    void runVoidGestisceIoException() {
        QuizException thrown = assertThrows(QuizException.class,
                () -> ExceptionShield.runVoid("salvataggio", () -> {
                    throw new IOException("disk full at " + INTERNAL_PATH);
                }));
        assertFalse(thrown.getMessage().contains(INTERNAL_PATH));
    }

    @Test
    void runVoidGestisceHappyPath() {
        assertDoesNotThrow(() -> ExceptionShield.runVoid("noop", () -> { /* no-op */ }));
    }
}
