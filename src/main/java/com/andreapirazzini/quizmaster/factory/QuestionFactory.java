package com.andreapirazzini.quizmaster.factory;

import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.exceptions.QuizLoadException;
import com.andreapirazzini.quizmaster.questions.MultipleChoiceQuestion;
import com.andreapirazzini.quizmaster.questions.OpenEndedQuestion;
import com.andreapirazzini.quizmaster.questions.TrueFalseQuestion;
import com.andreapirazzini.quizmaster.util.InputSanitizer;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Factory pattern. Centralizza la mappatura
 * {@code "type" JSON → classe Question concreta}. I chiamanti
 * (in pratica solo {@code QuizLoader}) non chiamano mai
 * {@code new MultipleChoiceQuestion(...)} direttamente.
 *
 * <p>Centralizzare qui ha due vantaggi:
 * <ol>
 *   <li>Un solo errore "type sconosciuto" sanitizzato, indipendente da dove
 *       il file YAML/JSON contenga il tipo sbagliato.</li>
 *   <li>Aggiungere un nuovo tipo (es. {@code "matching"}) tocca questo file
 *       e basta — il resto dell'app rimane invariato.</li>
 * </ol>
 */
public final class QuestionFactory {

    /**
     * Costruisce una {@link Question} concreta.
     *
     * @param type        case-insensitive: "multiple_choice" | "true_false" | "open_ended"
     * @param id          id univoco della domanda (dal JSON)
     * @param text        testo della domanda
     * @param basePoints  punti base (lo scoring strategy può scalarli)
     * @param params      parametri specifici per tipo (vedi javadoc dei costruttori)
     * @throws QuizLoadException se il tipo è sconosciuto o i parametri sono malformati
     */
    public Question create(String type, String id, String text, int basePoints,
                           Map<String, Object> params) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(params, "params");

        try {
            return switch (type.toLowerCase(Locale.ROOT)) {
                case "multiple_choice", "multiple-choice", "multi" ->
                        buildMultipleChoice(id, text, basePoints, params);
                case "true_false", "true-false", "boolean", "vf" ->
                        buildTrueFalse(id, text, basePoints, params);
                case "open_ended", "open-ended", "open", "text" ->
                        buildOpenEnded(id, text, basePoints, params);
                default -> throw new QuizLoadException(
                        "Tipo di domanda sconosciuto: '"
                                + InputSanitizer.sanitize(type) + "'.");
            };
        } catch (IllegalArgumentException e) {
            // Riconverte gli errori dei costruttori (parametri invalidi) in
            // un errore di caricamento già sanitizzato.
            throw new QuizLoadException(
                    "Parametri non validi per la domanda '" + id + "': "
                            + InputSanitizer.sanitize(e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private static MultipleChoiceQuestion buildMultipleChoice(String id, String text,
                                                              int basePoints,
                                                              Map<String, Object> params) {
        Object opt = params.get("options");
        if (!(opt instanceof List<?> optList)) {
            throw new QuizLoadException(
                    "Domanda '" + id + "': il campo 'options' deve essere una lista di stringhe.");
        }
        List<String> options = optList.stream().map(String::valueOf).toList();

        Object idx = params.get("correct_index");
        if (!(idx instanceof Number num)) {
            throw new QuizLoadException(
                    "Domanda '" + id + "': il campo 'correct_index' è obbligatorio (numero).");
        }

        return new MultipleChoiceQuestion(id, text, options, num.intValue(), basePoints);
    }

    private static TrueFalseQuestion buildTrueFalse(String id, String text, int basePoints,
                                                    Map<String, Object> params) {
        Object correct = params.get("correct_answer");
        if (!(correct instanceof Boolean b)) {
            throw new QuizLoadException(
                    "Domanda '" + id + "': il campo 'correct_answer' deve essere true/false.");
        }
        return new TrueFalseQuestion(id, text, b, basePoints);
    }

    private static OpenEndedQuestion buildOpenEnded(String id, String text, int basePoints,
                                                    Map<String, Object> params) {
        Object accepted = params.get("accepted_answers");
        if (!(accepted instanceof List<?> list)) {
            throw new QuizLoadException(
                    "Domanda '" + id + "': il campo 'accepted_answers' deve essere una lista di stringhe.");
        }
        List<String> answers = list.stream().map(String::valueOf).toList();
        return new OpenEndedQuestion(id, text, answers, basePoints);
    }
}
