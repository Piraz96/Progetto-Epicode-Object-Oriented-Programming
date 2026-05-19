package com.andreapirazzini.quizmaster.cli;

import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.events.QuizEventBus;
import com.andreapirazzini.quizmaster.scoring.ClassicScoring;
import com.andreapirazzini.quizmaster.scoring.ScoringStrategy;
import com.andreapirazzini.quizmaster.scoring.TimeBoundScoring;
import com.andreapirazzini.quizmaster.session.QuizSession;
import com.andreapirazzini.quizmaster.session.Scoreboard;
import com.andreapirazzini.quizmaster.session.SessionResult;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Menu interattivo principale. È l'orchestratore "lato UI" dell'app:
 * <pre>
 *   Menu principale → [1] Gioca → scegli quiz → scegli scoring → partita
 *                    → [2] Classifica
 *                    → [3] Statistiche
 *                    → [q] Esci
 * </pre>
 *
 * <p>Tutta la logica di rendering vive in {@link ConsoleUI}; tutta la
 * logica di esecuzione vive in {@link QuizSession}. Qui c'è solo
 * coordinamento.
 */
public final class Menu {

    private static final int DEFAULT_TIMER_SECONDS = 15;

    private final ConsoleUI ui;
    private final InputReader input;
    private final List<Quiz> quizzes;
    private final Scoreboard scoreboard;
    private final QuizEventBus bus;

    public Menu(ConsoleUI ui, InputReader input, List<Quiz> quizzes,
                Scoreboard scoreboard, QuizEventBus bus) {
        this.ui = ui;
        this.input = input;
        this.quizzes = List.copyOf(quizzes);
        this.scoreboard = scoreboard;
        this.bus = bus;
    }

    public void runLoop() {
        ui.banner();
        boolean running = true;
        while (running) {
            ui.mainMenu();
            String choice = ConsoleUI.normalizeChoice(input.readLine().orElse("q"));
            switch (choice) {
                case "1" -> playFlow();
                case "2" -> {
                    ui.leaderboard(scoreboard.top());
                    waitForEnter();
                }
                case "3" -> {
                    ui.stats(scoreboard.totalScoreByQuiz());
                    waitForEnter();
                }
                case "q", "quit", "exit" -> running = false;
                default -> ui.error("Scelta non riconosciuta: '" + choice + "'");
            }
        }
        ui.goodbye();
    }

    private void playFlow() {
        Optional<Quiz> quiz = chooseQuiz();
        if (quiz.isEmpty()) return;

        Optional<ScoringStrategy> strategy = chooseStrategy();
        if (strategy.isEmpty()) return;

        ui.dim("Hai " + DEFAULT_TIMER_SECONDS + " secondi per ogni domanda. Buona fortuna!");
        ui.newline();

        QuizSession session = new QuizSession(
                quiz.get(), strategy.get(), DEFAULT_TIMER_SECONDS,
                bus, input, System.nanoTime());
        SessionResult result = session.run();

        try {
            scoreboard.add(result);
        } catch (IOException e) {
            ui.error("Impossibile salvare nella classifica: " + e.getClass().getSimpleName());
        }

        ui.finalResult(result);
        waitForEnter();
    }

    private Optional<Quiz> chooseQuiz() {
        ui.section("Scegli un quiz");
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz q = quizzes.get(i);
            ui.info("  " + AnsiColor.bold("[" + (i + 1) + "]") + " "
                    + q.name() + AnsiColor.dim(" — " + q.totalQuestions() + " domande"));
        }
        ui.info("  " + AnsiColor.bold("[b]") + " indietro");
        ui.newline();
        ui.prompt("Scelta: ");

        String raw = ConsoleUI.normalizeChoice(input.readLine().orElse("b"));
        if (raw.equals("b") || raw.isEmpty()) return Optional.empty();
        try {
            int idx = Integer.parseInt(raw) - 1;
            if (idx < 0 || idx >= quizzes.size()) {
                ui.error("Numero fuori range");
                return Optional.empty();
            }
            return Optional.of(quizzes.get(idx));
        } catch (NumberFormatException e) {
            ui.error("Scelta non valida: " + raw);
            return Optional.empty();
        }
    }

    private Optional<ScoringStrategy> chooseStrategy() {
        List<ScoringStrategy> options = List.of(new ClassicScoring(), new TimeBoundScoring());
        ui.section("Scegli la modalità di punteggio");
        for (int i = 0; i < options.size(); i++) {
            ui.info("  " + AnsiColor.bold("[" + (i + 1) + "]") + " " + options.get(i).description());
        }
        ui.info("  " + AnsiColor.bold("[b]") + " indietro");
        ui.newline();
        ui.prompt("Scelta: ");

        String raw = ConsoleUI.normalizeChoice(input.readLine().orElse("b"));
        if (raw.equals("b") || raw.isEmpty()) return Optional.empty();
        try {
            int idx = Integer.parseInt(raw) - 1;
            if (idx < 0 || idx >= options.size()) {
                ui.error("Numero fuori range");
                return Optional.empty();
            }
            return Optional.of(options.get(idx));
        } catch (NumberFormatException e) {
            ui.error("Scelta non valida: " + raw);
            return Optional.empty();
        }
    }

    private void waitForEnter() {
        ui.newline();
        ui.prompt(AnsiColor.dim("Premi INVIO per tornare al menu... "));
        input.readLine();
    }
}
