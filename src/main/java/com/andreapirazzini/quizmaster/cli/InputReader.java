package com.andreapirazzini.quizmaster.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Legge righe da uno {@link InputStream} (tipicamente {@code System.in}) con
 * supporto al timeout. Implementato con un thread di background daemon che fa
 * la lettura bloccante; il main thread aspetta il risultato via {@link Future}
 * con timeout.
 *
 * <p>Se il timeout scade, la lettura sottostante rimane in attesa di un
 * carattere "qualunque". Per il flusso del quiz va bene: tra una domanda e
 * l'altra il prompt successivo viene comunque mostrato; un INVIO tardivo
 * sull'input precedente verrà raccolto come prima risposta della nuova
 * domanda. Questo è un trade-off accettato per non dover ricorrere a NIO
 * non-blocking.
 */
public final class InputReader {

    private final BufferedReader reader;
    private final ExecutorService readPool;

    public InputReader(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.readPool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "quizmaster-input");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Legge una riga, con timeout massimo.
     *
     * @return {@code Optional.empty()} se il timeout scade prima dell'input
     */
    public Optional<String> readLineWithTimeout(Duration timeout) {
        Future<String> future = readPool.submit(() -> {
            try {
                return reader.readLine();
            } catch (IOException e) {
                return null;
            }
        });
        try {
            String line = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return Optional.ofNullable(line);
        } catch (TimeoutException e) {
            // Volutamente NON cancello il future: cancel(true) tenterebbe un
            // interrupt che readLine() ignora. Il thread daemon finirà quando
            // la JVM termina, o quando la prossima readLine() raccoglie un
            // INVIO.
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException e) {
            return Optional.empty();
        }
    }

    /** Legge una riga senza timeout (blocca fino a INVIO o EOF). */
    public Optional<String> readLine() {
        try {
            return Optional.ofNullable(reader.readLine());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void shutdown() {
        readPool.shutdownNow();
    }
}
