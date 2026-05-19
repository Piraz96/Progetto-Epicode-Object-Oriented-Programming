package com.andreapirazzini.quizmaster.core;

/**
 * Composite-pattern root. Un {@code QuizElement} è una {@link Question} (foglia —
 * una singola domanda) oppure un {@link Quiz} / {@link Category} (composito che
 * aggrega altri elementi).
 *
 * <p>Sia foglie che compositi espongono la stessa API ({@link #title},
 * {@link #totalQuestions}), così il resto dell'app non deve mai chiedersi
 * "è una foglia o un composito?".
 */
public interface QuizElement {

    /** Titolo human-readable (mostrato nei menu). */
    String title();

    /**
     * Quante domande totali contiene questo elemento.
     * <ul>
     *   <li>Per una {@link Question} → 1</li>
     *   <li>Per un {@link Quiz} → somma delle question contenute</li>
     *   <li>Per una {@link Category} → somma dei quiz contenuti</li>
     * </ul>
     */
    int totalQuestions();

    /** {@code true} se l'elemento aggrega figli, {@code false} per le foglie. */
    default boolean isComposite() {
        return false;
    }
}
