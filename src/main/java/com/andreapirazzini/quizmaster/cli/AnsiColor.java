package com.andreapirazzini.quizmaster.cli;

/**
 * Costanti e helper per i codici ANSI di colore. Si disattivano automaticamente
 * se la env var {@code NO_COLOR} è settata (convenzione no-color.org), così
 * la stessa app gira bene anche in CI o su terminali che non supportano ANSI.
 *
 * <p>Centralizzata qui per evitare di sparpagliare {@code "[...m"} per
 * tutto il codice.
 */
public final class AnsiColor {

    private static final boolean ENABLED = System.getenv("NO_COLOR") == null;

    public static final String RESET   = ENABLED ? "[0m"  : "";
    public static final String BOLD    = ENABLED ? "[1m"  : "";
    public static final String DIM     = ENABLED ? "[2m"  : "";

    public static final String RED     = ENABLED ? "[31m" : "";
    public static final String GREEN   = ENABLED ? "[32m" : "";
    public static final String YELLOW  = ENABLED ? "[33m" : "";
    public static final String BLUE    = ENABLED ? "[34m" : "";
    public static final String MAGENTA = ENABLED ? "[35m" : "";
    public static final String CYAN    = ENABLED ? "[36m" : "";

    private AnsiColor() {
        // utility
    }

    public static String red(String s)     { return RED     + s + RESET; }
    public static String green(String s)   { return GREEN   + s + RESET; }
    public static String yellow(String s)  { return YELLOW  + s + RESET; }
    public static String blue(String s)    { return BLUE    + s + RESET; }
    public static String magenta(String s) { return MAGENTA + s + RESET; }
    public static String cyan(String s)    { return CYAN    + s + RESET; }
    public static String bold(String s)    { return BOLD    + s + RESET; }
    public static String dim(String s)     { return DIM     + s + RESET; }
}
