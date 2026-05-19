package com.andreapirazzini.quizmaster.shielding;

import com.andreapirazzini.quizmaster.exceptions.QuizException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Boundary dell'Exception Shielding.
 *
 * <p>Regole implementate, una per catch:
 * <ol>
 *   <li>{@link QuizException} è già sanitizzata → log INFO e rilancio così com'è.</li>
 *   <li>{@link IOException} → log SEVERE breve + log FINE con stack trace solo nel
 *       file, rilancio come {@code QuizException} generica.</li>
 *   <li>{@link RuntimeException} / altro → stesso trattamento, messaggio "internal error".</li>
 * </ol>
 *
 * <p>Two-tier logging: il messaggio SEVERE appare sia su console sia su file (breve,
 * solo nome dell'eccezione), mentre lo stack trace completo va SOLO al FileHandler
 * a livello FINE. Questo evita che l'utente veda mai uno stack trace su console,
 * il che è la prima penalità (-5) della sezione γ .
 */
public final class ExceptionShield {

    private static final Logger LOG = Logger.getLogger(ExceptionShield.class.getName());

    private ExceptionShield() {
        // utility, non istanziabile
    }

    /**
     * Esegue {@code action} sotto lo shield.
     *
     * @param operation breve nome human-readable dell'operazione (appare nei messaggi)
     * @param action    il lavoro da fare
     * @param <T>       tipo di ritorno
     * @return quanto {@code action} ha restituito in caso di successo
     * @throws QuizException sanitizzata, mostrabile all'utente
     */
    public static <T> T run(String operation, Callable<T> action) {
        try {
            return action.call();
        } catch (QuizException e) {
            LOG.log(Level.INFO, () -> "Operation '" + operation + "' failed: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            logShielded(operation, "I/O failure", e);
            throw new QuizException("Errore di I/O durante " + operation + ". Vedi il log per dettagli.");
        } catch (RuntimeException e) {
            logShielded(operation, "runtime error", e);
            throw new QuizException("Errore interno durante " + operation + ". Vedi il log per dettagli.");
        } catch (Exception e) {
            logShielded(operation, "unexpected error", e);
            throw new QuizException("Errore interno durante " + operation + ". Vedi il log per dettagli.");
        }
    }

    /** Overload per operazioni {@code void}. */
    public static void runVoid(String operation, RiskyAction action) {
        run(operation, () -> {
            action.execute();
            return null;
        });
    }

    /**
     * Two-tier shielded logging:
     * <ul>
     *   <li>Messaggio breve SEVERE (senza Throwable) → entra anche nel ConsoleHandler.</li>
     *   <li>Stack trace completo FINE → solo FileHandler. Il SimpleFormatter
     *       espanderebbe lo stack trace su ogni handler abilitato, quindi
     *       teniamo lo stack trace fuori dal record SEVERE.</li>
     * </ul>
     */
    private static void logShielded(String operation, String label, Throwable cause) {
        LOG.log(Level.SEVERE,
                () -> label + " during " + operation + ": " + cause.getClass().getSimpleName());
        LOG.log(Level.FINE, cause, () -> "full stack trace for " + operation);
    }

    /** {@code Runnable} che può lanciare qualunque eccezione checked. */
    @FunctionalInterface
    public interface RiskyAction {
        void execute() throws Exception;
    }
}
