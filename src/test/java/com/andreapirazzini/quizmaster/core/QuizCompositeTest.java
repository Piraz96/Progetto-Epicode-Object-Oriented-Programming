package com.andreapirazzini.quizmaster.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il contratto del Composite: Category contiene Quiz, Quiz contiene
 * Question — tre livelli annidati, totalQuestions() si propaga correttamente.
 */
class QuizCompositeTest {

    /** Question minimal per i test — sempre sbagliata. */
    private static final class StubQuestion extends Question {
        StubQuestion(String id) { super(id, "txt-" + id, 1); }
        @Override public boolean isCorrect(String userAnswer) { return false; }
        @Override public String correctAnswerText() { return "n/a"; }
        @Override public String prompt() { return text; }
    }

    @Test
    void totalQuestionsRicorsivoAttraversoTreLivelli() {
        Question q1 = new StubQuestion("q1");
        Question q2 = new StubQuestion("q2");
        Question q3 = new StubQuestion("q3");

        Quiz quizA = new Quiz("Quiz A", "due domande", List.of(q1, q2));
        Quiz quizB = new Quiz("Quiz B", "una domanda", List.of(q3));

        Category storia = new Category("Storia", List.of(quizA, quizB));

        assertEquals(1, q1.totalQuestions());
        assertEquals(2, quizA.totalQuestions());
        assertEquals(1, quizB.totalQuestions());
        assertEquals(3, storia.totalQuestions(),
                "Category deve sommare i totali dei quiz figli");
    }

    @Test
    void isCompositeDistingueFoglieDaiContenitori() {
        Question q = new StubQuestion("q");
        Quiz quiz = new Quiz("Q", "", List.of(q));
        Category cat = new Category("C", List.of(quiz));

        assertFalse(q.isComposite(), "Question è foglia");
        assertTrue(quiz.isComposite(), "Quiz è composito");
        assertTrue(cat.isComposite(), "Category è composito");
    }

    @Test
    void titleEspostoUniformemente() {
        Question q = new StubQuestion("q");
        Quiz quiz = new Quiz("Mio Quiz", "", List.of(q));
        Category cat = new Category("Mia Categoria", List.of(quiz));

        // Stesso metodo title() funziona su tutti — caller-side polymorphism.
        for (QuizElement el : List.of(q, quiz, cat)) {
            assertTrue(el.title() != null && !el.title().isBlank());
        }

        assertEquals("txt-q", q.title());
        assertEquals("Mio Quiz", quiz.title());
        assertEquals("Mia Categoria", cat.title());
    }
}
