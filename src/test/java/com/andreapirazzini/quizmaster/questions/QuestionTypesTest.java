package com.andreapirazzini.quizmaster.questions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionTypesTest {

    // ============ MultipleChoiceQuestion ============

    @Test
    void multipleChoice_accettaLetteraENumero() {
        MultipleChoiceQuestion q = new MultipleChoiceQuestion(
                "q1", "Capitale d'Italia?",
                List.of("Milano", "Roma", "Napoli"),
                1, 1);

        assertTrue(q.isCorrect("B"));
        assertTrue(q.isCorrect("b"));
        assertTrue(q.isCorrect("2"));
        assertFalse(q.isCorrect("A"));
        assertFalse(q.isCorrect("3"));
        assertFalse(q.isCorrect("X"));
        assertFalse(q.isCorrect(""));
        assertFalse(q.isCorrect(null));

        assertTrue(q.correctAnswerText().contains("Roma"));
        assertTrue(q.prompt().contains("A) Milano"));
        assertTrue(q.prompt().contains("C) Napoli"));
    }

    @Test
    void multipleChoice_rifiutaParametriInvalidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new MultipleChoiceQuestion("q", "?",
                        List.of("solo una"), 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new MultipleChoiceQuestion("q", "?",
                        List.of("A", "B"), 5, 1));
    }

    // ============ TrueFalseQuestion ============

    @Test
    void trueFalse_accettaTutteLeFormeComuni() {
        TrueFalseQuestion q = new TrueFalseQuestion("q", "La Terra è piatta", false, 1);

        for (String yes : new String[]{"V", "v", "vero", "TRUE", "true", "T", "sì", "si"}) {
            assertFalse(q.isCorrect(yes), "'" + yes + "' significa Vero, ma la risposta è Falso");
        }
        for (String no : new String[]{"F", "f", "falso", "FALSE", "no", "N"}) {
            assertTrue(q.isCorrect(no), "'" + no + "' significa Falso, e la risposta è Falso");
        }
        assertFalse(q.isCorrect("forse"));
        assertEquals("Falso", q.correctAnswerText());
    }

    // ============ OpenEndedQuestion ============

    @Test
    void openEnded_accettaVariantiECaseTolerantEAccent() {
        OpenEndedQuestion q = new OpenEndedQuestion("q",
                "Chi ha scritto la Divina Commedia?",
                List.of("Dante Alighieri", "Dante"), 1);

        assertTrue(q.isCorrect("Dante Alighieri"));
        assertTrue(q.isCorrect("dante alighieri"));
        assertTrue(q.isCorrect("  Dante  "));   // trim
        assertTrue(q.isCorrect("DANTE"));
        assertFalse(q.isCorrect("Petrarca"));
        assertFalse(q.isCorrect(""));
    }

    @Test
    void openEnded_ignoraAccenti() {
        OpenEndedQuestion q = new OpenEndedQuestion("q",
                "Esclamazione?", List.of("perché"), 1);

        assertTrue(q.isCorrect("perche"));    // senza accento
        assertTrue(q.isCorrect("PERCHÉ"));    // maiuscolo con accento
        assertTrue(q.isCorrect("Perche"));
    }

    @Test
    void openEnded_rifiutaListaVuota() {
        assertThrows(IllegalArgumentException.class,
                () -> new OpenEndedQuestion("q", "?", List.of(), 1));
    }
}
