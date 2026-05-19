package com.andreapirazzini.quizmaster.session;

import com.andreapirazzini.quizmaster.io.SafeFileReader;
import com.andreapirazzini.quizmaster.io.SafeFileWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Top-10 dei punteggi più alti, persistito in un file JSON locale.
 *
 * <p>Operazioni base: {@link #add(SessionResult)} aggiunge un punteggio,
 * riordina, taglia a 10 e salva. {@link #top()} restituisce la classifica
 * corrente. {@link #totalScoreByQuiz()} dimostra l'uso di Stream API +
 * Collectors per le statistiche.
 *
 * <p>Eventuali errori di lettura/scrittura sono visibili al chiamante come
 * {@link IOException} — verranno wrappati dal {@code ExceptionShield}.
 */
public final class Scoreboard {

    /** Numero massimo di entry tenute nella classifica. */
    public static final int MAX_ENTRIES = 10;

    private final Path file;
    private final List<SessionResult> entries;

    public Scoreboard(Path file) throws IOException {
        this.file = file;
        this.entries = new ArrayList<>(load(file));
    }

    /** Aggiunge un risultato, riordina, taglia a 10, salva su disco. */
    public synchronized void add(SessionResult result) throws IOException {
        entries.add(result);
        entries.sort(Comparator
                .comparingInt(SessionResult::finalScore).reversed()
                .thenComparing(SessionResult::playedAt));
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
        save();
    }

    /** Snapshot read-only della top-10 corrente. */
    public List<SessionResult> top() {
        return List.copyOf(entries);
    }

    /**
     * Statistica con Stream API: somma dei punteggi per ogni quiz nome.
     * (Solo per dimostrare i Collectors — utile in stats UI.)
     */
    public Map<String, Integer> totalScoreByQuiz() {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        SessionResult::quizName,
                        Collectors.summingInt(SessionResult::finalScore)));
    }

    /** Numero totale di partite registrate. */
    public int size() {
        return entries.size();
    }

    // ===== persistenza =====

    private static List<SessionResult> load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        String raw = SafeFileReader.readString(file);
        if (raw.isBlank()) {
            return List.of();
        }
        try {
            JSONArray arr = new JSONArray(raw);
            List<SessionResult> result = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                result.add(new SessionResult(
                        o.getString("quiz"),
                        o.getInt("score"),
                        o.getInt("correct"),
                        o.getInt("total"),
                        Instant.parse(o.getString("playedAt"))
                ));
            }
            return result;
        } catch (JSONException e) {
            // Scoreboard corrotta → riparti da zero piuttosto che far fallire l'app.
            return List.of();
        }
    }

    private void save() throws IOException {
        JSONArray arr = new JSONArray();
        for (SessionResult r : entries) {
            JSONObject o = new JSONObject();
            o.put("quiz", r.quizName());
            o.put("score", r.finalScore());
            o.put("correct", r.correctCount());
            o.put("total", r.totalQuestions());
            o.put("playedAt", r.playedAt().toString());
            arr.put(o);
        }
        SafeFileWriter.writeString(file, arr.toString(2));
    }
}
