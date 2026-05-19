package com.andreapirazzini.quizmaster.util;

/**
 * Pulisce stringhe in arrivo dall'utente o dal filesystem prima di
 * loggarle o usarle. Toglie caratteri di controllo (eccetto \r \n \t)
 * e tronca a 256 char.
 *
 * <p>Applicata a: input utente da terminale, valori letti da JSON, nomi
 * di file e messaggi di eccezione di terze parti che finirebbero nei log.
 */
public final class InputSanitizer {

    public static final int MAX_LEN = 256;

    private InputSanitizer() {
        // utility, non istanziabile
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String stripped = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        return stripped.length() > MAX_LEN ? stripped.substring(0, MAX_LEN) + "…" : stripped;
    }

    /** Versione "trim" che oltre a sanitize fa anche trim degli spazi. */
    public static String sanitizeTrim(String input) {
        return sanitize(input).trim();
    }
}
