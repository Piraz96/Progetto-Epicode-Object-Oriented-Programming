package com.andreapirazzini.quizmaster.loader;

import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.core.Quiz;
import com.andreapirazzini.quizmaster.exceptions.QuizLoadException;
import com.andreapirazzini.quizmaster.factory.QuestionFactory;
import com.andreapirazzini.quizmaster.io.SafeFileReader;
import com.andreapirazzini.quizmaster.util.InputSanitizer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser di file JSON quiz. Schema atteso:
 *
 * <pre>{@code
 * {
 *   "name": "Storia",
 *   "description": "Domande di storia generale",
 *   "questions": [
 *     {
 *       "id": "q1",
 *       "type": "multiple_choice",
 *       "text": "...",
 *       "points": 1,
 *       "params": { ... }
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>Errori di formato producono {@link QuizLoadException} con messaggi
 * sanitizzati. La factory si occupa di costruire le {@link Question} concrete.
 */
public final class QuizLoader {

    private final QuestionFactory factory;

    public QuizLoader() {
        this(new QuestionFactory());
    }

    public QuizLoader(QuestionFactory factory) {
        this.factory = factory;
    }

    /** Carica un singolo file JSON e restituisce il Quiz corrispondente. */
    public Quiz loadFile(Path jsonPath) throws IOException {
        String raw = SafeFileReader.readString(jsonPath);
        JSONObject root;
        try {
            root = new JSONObject(raw);
        } catch (JSONException e) {
            throw new QuizLoadException(
                    "File JSON non valido: " + InputSanitizer.sanitize(jsonPath.getFileName().toString()));
        }
        return parseQuiz(root, jsonPath.getFileName().toString());
    }

    /**
     * Carica tutti i file {@code *.json} di una cartella e restituisce la
     * lista dei Quiz, ordinati per nome.
     */
    public List<Quiz> loadDirectory(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new QuizLoadException(
                    "Cartella quiz non trovata: " + InputSanitizer.sanitize(dir.toString()));
        }
        List<Quiz> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path file : stream) {
                result.add(loadFile(file));
            }
        }
        result.sort(Comparator.comparing(Quiz::name));
        return result;
    }

    private Quiz parseQuiz(JSONObject root, String fileLabel) {
        String name = requireString(root, "name", fileLabel);
        String description = root.optString("description", "");

        JSONArray questionsArray;
        try {
            questionsArray = root.getJSONArray("questions");
        } catch (JSONException e) {
            throw new QuizLoadException(
                    "File '" + fileLabel + "': il campo 'questions' è obbligatorio e dev'essere un array.");
        }
        if (questionsArray.isEmpty()) {
            throw new QuizLoadException(
                    "File '" + fileLabel + "': il quiz deve contenere almeno una domanda.");
        }

        List<Question> questions = new ArrayList<>(questionsArray.length());
        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject q = questionsArray.optJSONObject(i);
            if (q == null) {
                throw new QuizLoadException(
                        "File '" + fileLabel + "': la domanda #" + (i + 1) + " non è un oggetto JSON.");
            }
            questions.add(parseQuestion(q, fileLabel));
        }

        return new Quiz(name, description, questions);
    }

    private Question parseQuestion(JSONObject node, String fileLabel) {
        String id = requireString(node, "id", fileLabel);
        String type = requireString(node, "type", fileLabel);
        String text = requireString(node, "text", fileLabel);
        int points = node.optInt("points", 1);

        Map<String, Object> params = new HashMap<>();
        if (node.has("params") && !node.isNull("params")) {
            JSONObject p = node.optJSONObject("params");
            if (p == null) {
                throw new QuizLoadException(
                        "File '" + fileLabel + "': il campo 'params' di '" + id + "' deve essere un oggetto.");
            }
            for (String key : p.keySet()) {
                params.put(key, javaValueOf(p.get(key)));
            }
        }

        return factory.create(type, id, text, points, params);
    }

    /** Converte ricorsivamente i valori org.json (JSONArray, etc.) in tipi Java standard. */
    private static Object javaValueOf(Object jsonValue) {
        if (jsonValue instanceof JSONArray arr) {
            List<Object> list = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                list.add(javaValueOf(arr.get(i)));
            }
            return list;
        }
        if (jsonValue instanceof JSONObject obj) {
            Map<String, Object> map = new HashMap<>();
            for (String key : obj.keySet()) {
                map.put(key, javaValueOf(obj.get(key)));
            }
            return map;
        }
        return jsonValue;  // String, Number, Boolean
    }

    private static String requireString(JSONObject node, String field, String fileLabel) {
        if (!node.has(field) || node.isNull(field)) {
            throw new QuizLoadException(
                    "File '" + fileLabel + "': il campo '" + field + "' è.");
        }
        return node.getString(field);
    }
}
