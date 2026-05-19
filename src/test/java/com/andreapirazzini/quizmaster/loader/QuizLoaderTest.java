package com.andreapirazzini.quizmaster.loader;

import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.exceptions.QuizLoadException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizLoaderTest {

    private static Path fixture(String name) throws URISyntaxException {
        return Paths.get(QuizLoaderTest.class.getResource("/fixtures/" + name).toURI());
    }

    @Test
    void caricaFileValido() throws IOException, URISyntaxException {
        Quiz q = new QuizLoader().loadFile(fixture("valid.json"));

        assertEquals("Fixture", q.name());
        assertEquals(2, q.totalQuestions());
        assertTrue(q.questions().get(0).isCorrect("B"));
        assertTrue(q.questions().get(1).isCorrect("V"));
    }

    @Test
    void filenameMancanteDelCampoNomeProduceQuizLoadException() throws URISyntaxException {
        Path file = fixture("missing-name.json");
        QuizLoadException ex = assertThrows(QuizLoadException.class,
                () -> new QuizLoader().loadFile(file));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void questionsVuotoProduceQuizLoadException() throws URISyntaxException {
        Path file = fixture("empty-questions.json");
        QuizLoadException ex = assertThrows(QuizLoadException.class,
                () -> new QuizLoader().loadFile(file));
        assertTrue(ex.getMessage().toLowerCase().contains("almeno una domanda"));
    }
}
