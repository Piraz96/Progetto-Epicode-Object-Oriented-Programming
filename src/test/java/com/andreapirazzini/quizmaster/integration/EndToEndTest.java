package com.andreapirazzini.quizmaster.integration;

import com.andreapirazzini.quizmaster.cli.InputReader;
import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventBus;
import com.andreapirazzini.quizmaster.loader.QuizLoader;
import com.andreapirazzini.quizmaster.questions.TrueFalseQuestion;
import com.andreapirazzini.quizmaster.scoring.ClassicScoring;
import com.andreapirazzini.quizmaster.session.QuizSession;
import com.andreapirazzini.quizmaster.session.SessionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test end-to-end: simula una partita completa al QuizSession con risposte
 * pre-registrate, e verifica lo scoring e gli eventi.
 */
class EndToEndTest {

    @Test
    void giocaQuizCompletoConClassicScoring() {
        // Tutte e 3 le domande sono True/False con risposta=true → risposta "V" è
        // sempre corretta, qualunque sia l'ordine deciso dallo ShuffleIterator.
        Quiz quiz = new Quiz("Test E2E", "tre domande", List.of(
                new TrueFalseQuestion("q1", "La Terra è rotonda", true, 1),
                new TrueFalseQuestion("q2", "L'acqua bolle a 100 gradi", true, 1),
                new TrueFalseQuestion("q3", "Roma è la capitale d'Italia", true, 2)
        ));

        // 3 INVII di "V" — tutte e 3 corrette indipendentemente dall'ordine.
        InputReader input = new InputReader(new ByteArrayInputStream(
                "V\nV\nV\n".getBytes(StandardCharsets.UTF_8)));

        QuizEventBus bus = new QuizEventBus();
        List<QuizEvent> events = new CopyOnWriteArrayList<>();
        bus.subscribe(events::add);

        try {
            QuizSession session = new QuizSession(
                    quiz, new ClassicScoring(), 5, bus, input, 0L);
            SessionResult result = session.run();

            // 3 corrette su 3, score = 1+1+2 = 4 (basePoints sommati)
            assertEquals(3, result.correctCount());
            assertEquals(3, result.totalQuestions());
            assertEquals(4, result.finalScore());
            assertEquals("Test E2E", result.quizName());
        } finally {
            input.shutdown();
        }

        // Verifica gli eventi: QuizStarted (1), QuestionPresented (3), QuestionScored (3), QuizCompleted (1)
        long started = events.stream().filter(e -> e instanceof QuizEvent.QuizStarted).count();
        long presented = events.stream().filter(e -> e instanceof QuizEvent.QuestionPresented).count();
        long scored = events.stream().filter(e -> e instanceof QuizEvent.QuestionScored).count();
        long completed = events.stream().filter(e -> e instanceof QuizEvent.QuizCompleted).count();

        assertEquals(1, started);
        assertEquals(3, presented);
        assertEquals(3, scored);
        assertEquals(1, completed);

        // Primo evento = QuizStarted, ultimo = QuizCompleted.
        assertTrue(events.get(0) instanceof QuizEvent.QuizStarted);
        assertTrue(events.get(events.size() - 1) instanceof QuizEvent.QuizCompleted);
    }

    @Test
    void rispostaSbagliataDaZeroPunti() {
        Quiz quiz = new Quiz("Test", "una domanda", List.of(
                new TrueFalseQuestion("q1", "?", true, 5)));

        // Risposta sbagliata.
        InputReader input = new InputReader(new ByteArrayInputStream(
                "F\n".getBytes(StandardCharsets.UTF_8)));

        try {
            QuizSession session = new QuizSession(
                    quiz, new ClassicScoring(), 5, new QuizEventBus(), input, 0L);
            SessionResult result = session.run();
            assertEquals(0, result.correctCount());
            assertEquals(0, result.finalScore());
        } finally {
            input.shutdown();
        }
    }

    @Test
    void caricaTuttiIQuizDallaCartellaQuizzes() throws IOException, URISyntaxException {
        // Punta alla cartella `quizzes/` nella root.
        // Per il test rt prendiamo il file valid.json già committato come fixture.
        Path validFixture = Paths.get(getClass().getResource("/fixtures/valid.json").toURI());
        Quiz q = new QuizLoader().loadFile(validFixture);
        assertTrue(q.totalQuestions() > 0);
    }
}
