package com.andreapirazzini.quizmaster.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardTest {

    private static SessionResult result(String quiz, int score) {
        return new SessionResult(quiz, score, 5, 5, Instant.now());
    }

    @Test
    void aggiungeOrdinaPunteggiInOrdineDecrescente(@TempDir Path tmp) throws IOException {
        Scoreboard sb = new Scoreboard(tmp.resolve("scoreboard.json"));
        sb.add(result("Storia", 10));
        sb.add(result("Cinema", 25));
        sb.add(result("Geografia", 17));

        List<SessionResult> top = sb.top();
        assertEquals(3, top.size());
        assertEquals(25, top.get(0).finalScore());
        assertEquals(17, top.get(1).finalScore());
        assertEquals(10, top.get(2).finalScore());
    }

    @Test
    void tieneSolo10Entries(@TempDir Path tmp) throws IOException {
        Scoreboard sb = new Scoreboard(tmp.resolve("scoreboard.json"));
        for (int i = 0; i < 15; i++) {
            sb.add(result("Q" + i, i));
        }
        assertEquals(10, sb.size());
        // Il punteggio più basso che è rimasto deve essere almeno 5 (15-10 = 5).
        assertTrue(sb.top().get(9).finalScore() >= 5);
    }

    @Test
    void persisteSuDisco(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("scoreboard.json");
        Scoreboard sb1 = new Scoreboard(file);
        sb1.add(result("Storia", 42));
        sb1.add(result("Cinema", 15));

        Scoreboard sb2 = new Scoreboard(file);
        assertEquals(2, sb2.size());
        assertEquals(42, sb2.top().get(0).finalScore());
        assertEquals("Storia", sb2.top().get(0).quizName());
    }

    @Test
    void totalScoreByQuizUsaStreams(@TempDir Path tmp) throws IOException {
        Scoreboard sb = new Scoreboard(tmp.resolve("scoreboard.json"));
        sb.add(result("Storia", 10));
        sb.add(result("Storia", 5));
        sb.add(result("Cinema", 20));

        Map<String, Integer> totals = sb.totalScoreByQuiz();
        assertEquals(15, totals.get("Storia"));
        assertEquals(20, totals.get("Cinema"));
    }
}
