package com.andreapirazzini.quizmaster.cli;

import com.andreapirazzini.quizmaster.session.SessionResult;

import java.util.List;
import java.util.Locale;

/**
 * Wrapper centralizzato su {@code System.out}. Tutte le stampe "di menu"
 * passano da qui — è una delle tre classi in cui {@code System.out} è
 * ammesso (le altre sono {@code QuizMasterApp.main} e
 * {@code ConsoleAnimationListener}, che è dentro al flusso del quiz).
 *
 * <p>Disegna banner, separatori, riquadri box-drawing, messaggi colorati.
 */
public final class ConsoleUI {

    private static final String BAR = "═".repeat(48);

    public void banner() {
        System.out.println();
        System.out.println(AnsiColor.cyan("╔" + BAR + "╗"));
        System.out.println(AnsiColor.cyan("║") + center(AnsiColor.bold("QUIZ MASTER") + AnsiColor.cyan("  v1.0"), 48) + AnsiColor.cyan("║"));
        System.out.println(AnsiColor.cyan("║") + center(AnsiColor.dim("gioca · sfida i tuoi amici · impara"), 48) + AnsiColor.cyan("║"));
        System.out.println(AnsiColor.cyan("╚" + BAR + "╝"));
        System.out.println();
    }

    public void section(String title) {
        System.out.println();
        System.out.println(AnsiColor.bold(AnsiColor.cyan("── " + title + " ──")));
    }

    public void info(String text) {
        System.out.println(text);
    }

    public void dim(String text) {
        System.out.println(AnsiColor.dim(text));
    }

    public void error(String text) {
        System.out.println(AnsiColor.red("⚠  " + text));
    }

    public void success(String text) {
        System.out.println(AnsiColor.green("✓  " + text));
    }

    /** Stampa il prompt SENZA newline (cursore resta sulla stessa riga). */
    public void prompt(String text) {
        System.out.print(text);
        System.out.flush();
    }

    public void newline() {
        System.out.println();
    }

    public void mainMenu() {
        section("Menu principale");
        info("  " + AnsiColor.bold("[1]") + " Gioca");
        info("  " + AnsiColor.bold("[2]") + " Mostra classifica");
        info("  " + AnsiColor.bold("[3]") + " Statistiche");
        info("  " + AnsiColor.bold("[q]") + " Esci");
        newline();
        prompt("Scelta: ");
    }

    public void leaderboard(List<SessionResult> top) {
        section("CLASSIFICA — Top 10");
        if (top.isEmpty()) {
            dim("(nessuna partita giocata ancora — gioca per primo!)");
            return;
        }
        info(String.format("  %-4s %-15s %6s %8s %12s",
                AnsiColor.bold("Pos"),
                AnsiColor.bold("Quiz"),
                AnsiColor.bold("Punti"),
                AnsiColor.bold("Risposte"),
                AnsiColor.bold("Quando")));
        info(AnsiColor.dim("  " + "─".repeat(50)));
        for (int i = 0; i < top.size(); i++) {
            SessionResult r = top.get(i);
            String medal = switch (i) {
                case 0 -> AnsiColor.yellow("🥇");
                case 1 -> AnsiColor.dim("🥈");
                case 2 -> AnsiColor.yellow("🥉");
                default -> "  ";
            };
            info(String.format("  %s%-2d %-15s %6d %5d/%-3d %12s",
                    medal, i + 1,
                    truncate(r.quizName(), 15),
                    r.finalScore(),
                    r.correctCount(), r.totalQuestions(),
                    r.playedAt().toString().substring(0, 10)));
        }
    }

    public void stats(java.util.Map<String, Integer> totalsByQuiz) {
        section("STATISTICHE — Punti totali per quiz");
        if (totalsByQuiz.isEmpty()) {
            dim("(nessuna statistica disponibile)");
            return;
        }
        totalsByQuiz.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> info(String.format("  %-15s %s", e.getKey(),
                        AnsiColor.yellow(String.valueOf(e.getValue())))));
    }

    public void finalResult(SessionResult r) {
        newline();
        section("RIEPILOGO PARTITA");
        info("  Quiz:          " + AnsiColor.bold(r.quizName()));
        info("  Punteggio:     " + AnsiColor.yellow(String.valueOf(r.finalScore())));
        info("  Corrette:      " + r.correctCount() + " / " + r.totalQuestions()
                + "  (" + r.percentage() + "%)");
        newline();
        if (r.percentage() >= 80) {
            success("Grande! Ottimo risultato!");
        } else if (r.percentage() >= 50) {
            info(AnsiColor.cyan("Niente male, puoi riprovare per migliorare."));
        } else {
            info(AnsiColor.yellow("Eh, c'è margine di miglioramento — riprova!"));
        }
        newline();
    }

    public void goodbye() {
        newline();
        info(AnsiColor.cyan("Alla prossima! 👋"));
        newline();
    }

    // ===== helper =====

    private static String center(String content, int width) {
        // Strip ONLY i codici ANSI proper (ESC + '[' + digits/separators + 'm')
        // per misurare la lunghezza visibile.
        int visible = content.replaceAll("\\[[0-9;]*m", "").length();
        int pad = Math.max(0, (width - visible) / 2);
        int rightPad = Math.max(0, width - visible - pad);
        return " ".repeat(pad) + content + " ".repeat(rightPad);
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }

    /** Lettura case-insensitive di una scelta numerica con fallback. */
    public static String normalizeChoice(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(Locale.ROOT);
    }
}
