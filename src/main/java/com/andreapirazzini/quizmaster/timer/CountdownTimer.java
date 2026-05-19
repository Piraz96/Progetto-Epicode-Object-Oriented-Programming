package com.andreapirazzini.quizmaster.timer;

import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventBus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multithreading.
 *
 * <p>Un timer countdown che gira su un thread di background, pubblica un
 * {@link QuizEvent.TimerTick} ogni secondo sul {@link QuizEventBus} e un
 * {@link QuizEvent.TimerExpired} alla scadenza.
 *
 * <p>Note di progettazione:
 * <ul>
 *   <li><b>Fixed pool</b> da 1 thread daemon (no leak: i daemon non bloccano
 *       lo shutdown della JVM se il main esce inaspettatamente).</li>
 *   <li><b>Cancellazione race-free</b> via {@link AtomicBoolean}: il main
 *       thread chiama {@link #cancel()} quando l'utente risponde in tempo;
 *       il prossimo tick legge il flag e esce subito senza pubblicare nulla.</li>
 *   <li><b>{@link AtomicInteger} per il contatore</b>: anche se il task gira
 *       su un singolo thread, l'atomic rende l'API testabile in lettura da
 *       qualunque thread (es. UI che vuole sapere quanti secondi rimangono).</li>
 *   <li><b>{@link #shutdown()} idempotente</b> in {@code finally} dal chiamante:
 *       il pool si chiude anche se la domanda fallisce con un'eccezione.</li>
 * </ul>
 */
public final class CountdownTimer {

    private final int durationSeconds;
    private final QuizEventBus bus;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean expired = new AtomicBoolean(false);
    private final AtomicInteger remaining;
    private ScheduledFuture<?> future;

    public CountdownTimer(int durationSeconds, QuizEventBus bus) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds deve essere > 0");
        }
        this.durationSeconds = durationSeconds;
        this.bus = bus;
        this.remaining = new AtomicInteger(durationSeconds);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "quizmaster-timer");
            t.setDaemon(true);
            return t;
        });
    }

    /** Avvia il countdown. Ritorna subito; il timer gira in background. */
    public synchronized void start() {
        if (future != null) {
            throw new IllegalStateException("Timer già avviato");
        }
        future = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        if (cancelled.get() || expired.get()) {
            return;
        }
        int left = remaining.getAndDecrement();
        if (left <= 0) {
            expired.set(true);
            bus.publish(new QuizEvent.TimerExpired());
            return;
        }
        bus.publish(new QuizEvent.TimerTick(left));
    }

    /**
     * Ferma il timer: nessun altro tick verrà pubblicato. Idempotente.
     * Tipicamente invocato dal main thread quando l'utente risponde.
     */
    public void cancel() {
        cancelled.set(true);
    }

    /**
     * Libera il thread pool. Idempotente. Sempre invocata in un {@code finally}
     * dal chiamante per evitare leak.
     */
    public void shutdown() {
        cancel();
        scheduler.shutdownNow();
    }

    /** Vero se il countdown è arrivato naturalmente a 0. */
    public boolean isExpired() {
        return expired.get();
    }

    /** Vero se {@link #cancel()} è stato chiamato. */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /** Secondi rimanenti (snapshot). Negativo dopo la scadenza, irrilevante. */
    public int remainingSeconds() {
        return Math.max(0, remaining.get());
    }

    public int durationSeconds() {
        return durationSeconds;
    }
}
