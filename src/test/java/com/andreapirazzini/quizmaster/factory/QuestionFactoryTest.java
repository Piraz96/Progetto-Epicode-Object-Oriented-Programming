package com.andreapirazzini.quizmaster.factory;

import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.exceptions.QuizLoadException;
import com.andreapirazzini.quizmaster.questions.MultipleChoiceQuestion;
import com.andreapirazzini.quizmaster.questions.OpenEndedQuestion;
import com.andreapirazzini.quizmaster.questions.TrueFalseQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionFactoryTest {

    private final QuestionFactory factory = new QuestionFactory();

    @Test
    void costruisceMultipleChoice() {
        Question q = factory.create("multiple_choice", "q1", "Capitale?",
                1, Map.of(
                        "options", List.of("Roma", "Milano"),
                        "correct_index", 0));
        assertInstanceOf(MultipleChoiceQuestion.class, q);
        assertTrue(q.isCorrect("A"));
    }

    @Test
    void costruisceTrueFalse() {
        Question q = factory.create("true_false", "q2", "La Terra è rotonda",
                1, Map.of("correct_answer", true));
        assertInstanceOf(TrueFalseQuestion.class, q);
        assertTrue(q.isCorrect("V"));
    }

    @Test
    void costruisceOpenEnded() {
        Question q = factory.create("open_ended", "q3", "Autore Divina Commedia?",
                1, Map.of("accepted_answers", List.of("Dante", "Dante Alighieri")));
        assertInstanceOf(OpenEndedQuestion.class, q);
        assertTrue(q.isCorrect("dante"));
    }

    @Test
    void caseInsensitiveSulTipo() {
        Question q = factory.create("Multiple_Choice", "q", "?",
                1, Map.of("options", List.of("a", "b"), "correct_index", 1));
        assertInstanceOf(MultipleChoiceQuestion.class, q);
    }

    @Test
    void tipoSconosciutoProduceQuizLoadExceptionSanitizzata() {
        QuizLoadException ex = assertThrows(QuizLoadException.class,
                () -> factory.create("essay", "q", "?", 1, Map.of()));
        assertTrue(ex.getMessage().contains("essay"));
        assertTrue(ex.getMessage().startsWith("Tipo di domanda sconosciuto"));
    }

    @Test
    void parametriMancantiProduconoQuizLoadException() {
        // multiple_choice senza 'options'
        assertThrows(QuizLoadException.class,
                () -> factory.create("multiple_choice", "q", "?",
                        1, Map.of("correct_index", 0)));

        // true_false senza 'correct_answer'
        assertThrows(QuizLoadException.class,
                () -> factory.create("true_false", "q", "?", 1, Map.of()));

        // open_ended senza 'accepted_answers'
        assertThrows(QuizLoadException.class,
                () -> factory.create("open_ended", "q", "?", 1, Map.of()));
    }

    @Test
    void parametriMalformatiVengonoTradottiInQuizLoadException() {
        // correct_index fuori range → IllegalArgumentException dal costruttore → wrapped
        QuizLoadException ex = assertThrows(QuizLoadException.class,
                () -> factory.create("multiple_choice", "q", "?",
                        1, Map.of("options", List.of("a", "b"), "correct_index", 99)));
        assertTrue(ex.getMessage().toLowerCase().contains("non validi"));
    }
}
