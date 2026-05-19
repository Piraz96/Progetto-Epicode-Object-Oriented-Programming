package com.andreapirazzini.quizmaster;

import com.andreapirazzini.quizmaster.cli.ConsoleUI;
import com.andreapirazzini.quizmaster.cli.InputReader;
import com.andreapirazzini.quizmaster.cli.Menu;
import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.events.QuizEventBus;
import com.andreapirazzini.quizmaster.events.listeners.ConsoleAnimationListener;
import com.andreapirazzini.quizmaster.exceptions.QuizException;
import com.andreapirazzini.quizmaster.loader.QuizLoader;
import com.andreapirazzini.quizmaster.logging.LoggingConfig;
import com.andreapirazzini.quizmaster.session.Scoreboard;
import com.andreapirazzini.quizmaster.shielding.ExceptionShield;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Entry point. Wiring "a mano" (no DI framework), tutto sotto
 * {@link ExceptionShield} così qualunque eccezione finisce in un messaggio
 * sanitizzato + exit code 1, mai uno stack trace su console.
 *
 * <p>Argomenti CLI:
 * <ul>
 *   <li>nessuno: usa la cartella {@code ./quizzes/} per i quiz e
 *       {@code ./scoreboard.json} per la classifica.</li>
 *   <li>{@code <dir>}: usa la dir indicata al posto di {@code ./quizzes/}.</li>
 *   <li>{@code -h} / {@code --help}: mostra usage ed esce.</li>
 * </ul>
 */
public final class QuizMasterApp {

    private static final String DEFAULT_QUIZZES_DIR = "quizzes";
    private static final String DEFAULT_SCOREBOARD_FILE = "scoreboard.json";

    private QuizMasterApp() {
        // entry point, non istanziabile
    }

    public static void main(String[] args) {
        LoggingConfig.init();

        if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
            System.out.println(usage());
            return;
        }

        Path quizzesDir = Paths.get(args.length > 0 ? args[0] : DEFAULT_QUIZZES_DIR);
        Path scoreboardFile = Paths.get(DEFAULT_SCOREBOARD_FILE);

        try {
            ExceptionShield.runVoid("avvio QuizMaster", () -> runApp(quizzesDir, scoreboardFile));
        } catch (QuizException e) {
            // Unico punto in cui un'eccezione user-facing viene mostrata.
            // Niente stack trace: solo il messaggio sanitizzato.
            System.err.println("QuizMaster: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runApp(Path quizzesDir, Path scoreboardFile) throws Exception {
        List<Quiz> quizzes = new QuizLoader().loadDirectory(quizzesDir);
        if (quizzes.isEmpty()) {
            throw new QuizException("Nessun file quiz trovato in '" + quizzesDir + "'.");
        }

        Scoreboard scoreboard = new Scoreboard(scoreboardFile);

        QuizEventBus bus = new QuizEventBus();
        bus.subscribe(new ConsoleAnimationListener());

        ConsoleUI ui = new ConsoleUI();
        InputReader input = new InputReader(System.in);
        try {
            Menu menu = new Menu(ui, input, quizzes, scoreboard, bus);
            menu.runLoop();
        } finally {
            input.shutdown();
        }
    }

    private static String usage() {
        return """
                QuizMaster — quiz interattivo CLI

                Uso:  java -jar quizmaster.jar [dir-quizzes]

                Argomenti:
                  dir-quizzes   cartella che contiene i file *.json (default: ./quizzes/)
                  -h, --help    mostra questo aiuto

                Variabili d'ambiente:
                  NO_COLOR      se impostata, disabilita i colori ANSI

                Output:
                  scoreboard.json    classifica top-10 (creata/aggiornata)
                  quizmaster.log     log dettagliato (rotato)
                """;
    }
}
