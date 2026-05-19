package com.andreapirazzini.quizmaster.session;

import com.andreapirazzini.quizmaster.cli.InputReader;
import com.andreapirazzini.quizmaster.core.Answer;
import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.core.QuestionResult;
import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventBus;
import com.andreapirazzini.quizmaster.events.listeners.StatsCollectorListener;
import com.andreapirazzini.quizmaster.iterator.ShuffleIterator;
import com.andreapirazzini.quizmaster.scoring.ScoringStrategy;
import com.andreapirazzini.quizmaster.timer.CountdownTimer;
import com.andreapirazzini.quizmaster.util.InputSanitizer;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Orchestratore di un singolo quiz. Mette insieme:
 * <ul>
 *   <li>{@link ShuffleIterator} per l'ordine delle domande (Iterator pattern),</li>
 *   <li>{@link CountdownTimer} per il timer di ogni domanda (multithreading),</li>
 *   <li>{@link InputReader} per leggere la risposta utente con timeout,</li>
 *   <li>{@link ScoringStrategy} per assegnare i punti (Strategy pattern),</li>
 *   <li>{@link QuizEventBus} per notificare tutto a chi guarda (Observer pattern).</li>
 * </ul>
 *
 * <p>Una sessione è un oggetto "una tantum": {@link #run()} si chiama una sola
 * volta e restituisce il {@link SessionResult} finale.
 */
public final class QuizSession {

    private final Quiz quiz;
    private final ScoringStrategy scoring;
    private final int timerSeconds;
    private final QuizEventBus bus;
    private final InputReader inputReader;
    private final long shuffleSeed;

    public QuizSession(Quiz quiz, ScoringStrategy scoring, int timerSeconds,
                       QuizEventBus bus, InputReader inputReader, long shuffleSeed) {
        this.quiz = Objects.requireNonNull(quiz, "quiz");
        this.scoring = Objects.requireNonNull(scoring, "scoring");
        if (timerSeconds <= 0) {
            throw new IllegalArgumentException("timerSeconds deve essere > 0");
        }
        this.timerSeconds = timerSeconds;
        this.bus = Objects.requireNonNull(bus, "bus");
        this.inputReader = Objects.requireNonNull(inputReader, "inputReader");
        this.shuffleSeed = shuffleSeed;
    }

    public SessionResult run() {
        // Listener interno per accumulare gli score senza esportare lo stato.
        StatsCollectorListener stats = new StatsCollectorListener();
        bus.subscribe(stats);

        bus.publish(new QuizEvent.QuizStarted(quiz.name(), quiz.totalQuestions()));

        ShuffleIterator iterator = new ShuffleIterator(quiz.questions(), shuffleSeed);
        int index = 1;
        while (iterator.hasNext()) {
            Question q = iterator.next();
            bus.publish(new QuizEvent.QuestionPresented(index, quiz.totalQuestions(), q));

            CountdownTimer timer = new CountdownTimer(timerSeconds, bus);
            long startNanos = System.nanoTime();
            Answer answer;
            try {
                timer.start();
                Optional<String> input = inputReader.readLineWithTimeout(
                        Duration.ofSeconds(timerSeconds));
                Duration elapsed = Duration.ofNanos(System.nanoTime() - startNanos);
                if (input.isEmpty() || timer.isExpired()) {
                    answer = Answer.timeout(elapsed);
                } else {
                    answer = new Answer(InputSanitizer.sanitizeTrim(input.get()), elapsed);
                }
            } finally {
                timer.cancel();
                timer.shutdown();
            }

            int secondsLeft = Math.max(0, timerSeconds - (int) answer.timeTaken().getSeconds());
            bus.publish(new QuizEvent.AnswerGiven(answer));

            int points = scoring.score(q, answer, secondsLeft, timerSeconds);
            QuestionResult<String> result = points > 0
                    ? QuestionResult.correct(q.id(), points, answer.value())
                    : QuestionResult.wrong(q.id(), answer.value());
            bus.publish(new QuizEvent.QuestionScored(result));

            index++;
        }

        bus.publish(new QuizEvent.QuizCompleted(
                stats.totalScore(), quiz.totalQuestions(), stats.correctCount()));

        return new SessionResult(
                quiz.name(),
                stats.totalScore(),
                stats.correctCount(),
                quiz.totalQuestions(),
                Instant.now());
    }
}
