package com.andreapirazzini.quizmaster.iterator;

import com.andreapirazzini.quizmaster.core.Question;
import com.andreapirazzini.quizmaster.questions.TrueFalseQuestion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShuffleIteratorTest {

    private static List<Question> dieci() {
        return IntStream.range(0, 10)
                .mapToObj(i -> (Question) new TrueFalseQuestion(
                        "q" + i, "test " + i, true, 1))
                .toList();
    }

    @Test
    void seedDeterministicoProduceStessaPermutazione() {
        ShuffleIterator it1 = new ShuffleIterator(dieci(), 42L);
        ShuffleIterator it2 = new ShuffleIterator(dieci(), 42L);

        List<String> seq1 = drain(it1);
        List<String> seq2 = drain(it2);

        assertEquals(seq1, seq2, "lo stesso seed deve produrre lo stesso ordine");
    }

    @Test
    void seedDiversiProduconoOrdiniDiversi() {
        List<String> seq1 = drain(new ShuffleIterator(dieci(), 1L));
        List<String> seq2 = drain(new ShuffleIterator(dieci(), 2L));

        assertNotEquals(seq1, seq2, "seed diversi devono dare ordini diversi");
    }

    @Test
    void emetteOgniDomandaUnaSolaVolta() {
        ShuffleIterator it = new ShuffleIterator(dieci(), 100L);
        List<String> seq = drain(it);

        assertEquals(10, seq.size());
        assertEquals(10, seq.stream().distinct().count(),
                "ogni id deve apparire esattamente una volta");
    }

    @Test
    void nextSuIteratoreEsauritoSollevaNoSuchElement() {
        ShuffleIterator it = new ShuffleIterator(
                List.of(new TrueFalseQuestion("solo", "?", true, 1)), 0L);
        it.next();

        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void contatoriRiflettonoLoStato() {
        ShuffleIterator it = new ShuffleIterator(dieci(), 0L);
        assertEquals(10, it.totalCount());
        assertEquals(10, it.remaining());
        assertEquals(0, it.emittedCount());

        it.next();
        it.next();

        assertEquals(2, it.emittedCount());
        assertEquals(8, it.remaining());
        assertTrue(it.hasNext());
    }

    private static List<String> drain(ShuffleIterator it) {
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next().id());
        }
        return result;
    }
}
