package com.andreapirazzini.quizmaster.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer pattern. Bus pub/sub in-process.
 *
 * <p>Backing {@link CopyOnWriteArrayList} perché:
 * <ul>
 *   <li>la {@code publish} viene chiamata anche da thread di background (timer);</li>
 *   <li>la lista di listener può cambiare durante un quiz (raro ma possibile);</li>
 *   <li>il costo della copy-on-write è trascurabile rispetto al lavoro dei listener.</li>
 * </ul>
 */
public final class QuizEventBus {

    private final List<QuizEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(QuizEventListener listener) {
        listeners.add(listener);
    }

    public void publish(QuizEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }

    public int listenerCount() {
        return listeners.size();
    }
}
