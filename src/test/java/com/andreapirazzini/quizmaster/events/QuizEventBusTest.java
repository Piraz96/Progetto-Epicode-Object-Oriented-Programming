package com.andreapirazzini.quizmaster.events;

import com.andreapirazzini.quizmaster.core.QuestionResult;
import com.andreapirazzini.quizmaster.events.listeners.StatsCollectorListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizEventBusTest {

    @Test
    void consegnaAGliEventiInOrdineAITuttiIListener() {
        QuizEventBus bus = new QuizEventBus();
        List<String> log = new ArrayList<>();

        bus.subscribe(e -> log.add("L1:" + describe(e)));
        bus.subscribe(e -> log.add("L2:" + describe(e)));

        bus.publish(new QuizEvent.QuizStarted("Test", 1));
        bus.publish(new QuizEvent.QuizCompleted(3, 1, 1));

        assertEquals(List.of(
                "L1:Started(Test,1)", "L2:Started(Test,1)",
                "L1:Completed(3,1,1)", "L2:Completed(3,1,1)"
        ), log);
    }

    @Test
    void statsCollectorAccumulaCorretti() {
        QuizEventBus bus = new QuizEventBus();
        StatsCollectorListener stats = new StatsCollectorListener();
        bus.subscribe(stats);

        bus.publish(new QuizEvent.QuestionScored(QuestionResult.correct("q1", 2, "A")));
        bus.publish(new QuizEvent.QuestionScored(QuestionResult.correct("q2", 1, "B")));
        bus.publish(new QuizEvent.QuestionScored(QuestionResult.wrong("q3", "C")));

        assertEquals(2, stats.correctCount());
        assertEquals(1, stats.wrongCount());
        assertEquals(3, stats.totalScore());
    }

    private static String describe(QuizEvent e) {
        return switch (e) {
            case QuizEvent.QuizStarted s ->
                    "Started(" + s.quizName() + "," + s.totalQuestions() + ")";
            case QuizEvent.QuizCompleted c ->
                    "Completed(" + c.finalScore() + "," + c.totalQuestions() + "," + c.correctCount() + ")";
            default -> e.getClass().getSimpleName();
        };
    }
}
