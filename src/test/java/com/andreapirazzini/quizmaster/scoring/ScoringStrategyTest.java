package com.andreapirazzini.quizmaster.scoring;

import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.questions.TrueFalseQuestion;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoringStrategyTest {

    private static final Question DOMANDA = new TrueFalseQuestion("q", "?", true, 2);

    @Test
    void classicScoring_correttoDaBasePoints() {
        ScoringStrategy s = new ClassicScoring();
        Answer giusta = new Answer("V", Duration.ofSeconds(3));
        Answer sbagliata = new Answer("F", Duration.ofSeconds(3));

        assertEquals(2, s.score(DOMANDA, giusta, 10, 15));
        assertEquals(0, s.score(DOMANDA, sbagliata, 10, 15));
        // Il tempo non influenza il classic.
        assertEquals(2, s.score(DOMANDA, giusta, 0, 15));
        assertEquals(2, s.score(DOMANDA, giusta, 15, 15));
    }

    @Test
    void classicScoring_timeoutDaZero() {
        Answer timeout = Answer.timeout(Duration.ofSeconds(15));
        assertEquals(0, new ClassicScoring().score(DOMANDA, timeout, 0, 15));
    }

    @Test
    void timeBoundScoring_premiaVelocita() {
        ScoringStrategy s = new TimeBoundScoring();
        Answer giusta = new Answer("V", Duration.ofSeconds(5));

        // Rispondendo subito (15 secondi rimasti su 15)
        assertEquals(2 + 15, s.score(DOMANDA, giusta, 15, 15));
        // Rispondendo a metà
        assertEquals(2 + 7, s.score(DOMANDA, giusta, 7, 15));
        // Rispondendo all'ultimo secondo
        assertEquals(2, s.score(DOMANDA, giusta, 0, 15));
    }

    @Test
    void timeBoundScoring_sbagliatoTimeoutDanno0() {
        ScoringStrategy s = new TimeBoundScoring();
        assertEquals(0, s.score(DOMANDA,
                new Answer("F", Duration.ofSeconds(3)), 10, 15));
        assertEquals(0, s.score(DOMANDA,
                Answer.timeout(Duration.ofSeconds(15)), 0, 15));
    }

    @Test
    void nomeEDescrizioneNonVuoti() {
        for (ScoringStrategy s : new ScoringStrategy[]{new ClassicScoring(), new TimeBoundScoring()}) {
            assertEquals(s.name(), s.name().trim());
            assertEquals(s.description(), s.description().trim());
            // assert non-empty
            assertEquals(false, s.name().isBlank());
            assertEquals(false, s.description().isBlank());
        }
    }
}
