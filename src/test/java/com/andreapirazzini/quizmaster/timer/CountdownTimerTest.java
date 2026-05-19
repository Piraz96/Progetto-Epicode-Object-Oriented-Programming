package com.andreapirazzini.quizmaster.timer;

import com.andreapirazzini.quizmaster.events.QuizEvent;
import com.andreapirazzini.quizmaster.events.QuizEventBus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountdownTimerTest {

    @Test
    void scadenzaNaturalePubblicaTimerExpired() throws InterruptedException {
        QuizEventBus bus = new QuizEventBus();
        List<QuizEvent> events = new CopyOnWriteArrayList<>();
        CountDownLatch expired = new CountDownLatch(1);

        bus.subscribe(e -> {
            events.add(e);
            if (e instanceof QuizEvent.TimerExpired) {
                expired.countDown();
            }
        });

        CountdownTimer timer = new CountdownTimer(1, bus);
        try {
            timer.start();
            assertTrue(expired.await(3, TimeUnit.SECONDS),
                    "TimerExpired non è stato pubblicato entro 3 secondi");
            assertTrue(timer.isExpired());
            assertFalse(timer.isCancelled());

            // Almeno un Tick + un Expired.
            long ticks = events.stream().filter(e -> e instanceof QuizEvent.TimerTick).count();
            long expiries = events.stream().filter(e -> e instanceof QuizEvent.TimerExpired).count();
            assertTrue(ticks >= 1, "atteso almeno un Tick");
            assertEquals(1, expiries, "atteso esattamente un Expired");
        } finally {
            timer.shutdown();
        }
    }

    @Test
    void cancelPrecoceFermaIlTimer() throws InterruptedException {
        QuizEventBus bus = new QuizEventBus();
        List<QuizEvent> events = new CopyOnWriteArrayList<>();
        bus.subscribe(events::add);

        CountdownTimer timer = new CountdownTimer(10, bus);
        try {
            timer.start();
            Thread.sleep(50);  // qualche ms per far partire il primo tick
            timer.cancel();

            // Aspettiamo abbondantemente per assicurarci che NON arrivi TimerExpired.
            Thread.sleep(1500);

            assertFalse(timer.isExpired(), "timer cancellato non deve marcare expired");
            assertTrue(timer.isCancelled());
            long expiries = events.stream().filter(e -> e instanceof QuizEvent.TimerExpired).count();
            assertEquals(0, expiries, "dopo cancel non deve arrivare TimerExpired");
        } finally {
            timer.shutdown();
        }
    }

    @Test
    void shutdownIdempotente() {
        QuizEventBus bus = new QuizEventBus();
        CountdownTimer timer = new CountdownTimer(5, bus);
        timer.shutdown();
        timer.shutdown();  // secondo invocazione: nessun crash
    }

    @Test
    void rifiutaDurataInvalida() {
        QuizEventBus bus = new QuizEventBus();
        assertThrows(IllegalArgumentException.class, () -> new CountdownTimer(0, bus));
        assertThrows(IllegalArgumentException.class, () -> new CountdownTimer(-1, bus));
    }

    @Test
    void doppioStartLanciaIllegalState() {
        QuizEventBus bus = new QuizEventBus();
        CountdownTimer timer = new CountdownTimer(5, bus);
        try {
            timer.start();
            assertThrows(IllegalStateException.class, timer::start);
        } finally {
            timer.shutdown();
        }
    }
}
