package service;

import domain.Entry;
import domain.Posting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static config.Config.DEFAULT_PATHS;
import static org.junit.jupiter.api.Assertions.*;

class MasterNodeTest {

    @Test
    void buildIndexFromSource() {
        MasterNode master = new MasterNode();
        var simpleInvertedIndex = master.buildIndexFromSource(List.of(DEFAULT_PATHS.getFirst()), 1, 1);
        String surnamePresentInTheFirstFile = "Costner";
        surnamePresentInTheFirstFile = (new TextProcessor().processText(surnamePresentInTheFirstFile))[0]; //adjust word to token
        var postings = simpleInvertedIndex.get(surnamePresentInTheFirstFile);
        assertFalse(postings.isEmpty());
        assertTrue(postings.stream().anyMatch(p -> p.docId() == 0 && p.folder() == 0 && p.positions().getFirst() == 3));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 2, 5, 8, 10})
    void testPutOfParallelIndex(int threadNumber) {
        //given
        MasterNode master = new MasterNode();
        long start = System.currentTimeMillis();
        var simpleInvertedIndex = master.buildIndexFromSource(List.of(DEFAULT_PATHS.getFirst()), 1, 1);
        final long singleTime = System.currentTimeMillis() - start;
        //when
        start = System.currentTimeMillis();
        var parallelIndex = master.buildIndexFromSource(List.of(DEFAULT_PATHS.getFirst()), 1, threadNumber);
        final long parallelTime = System.currentTimeMillis() - start;
        //then
        System.out.println(STR. """
                Single thread time: \{ singleTime }
                \{ threadNumber } threads time: \{ parallelTime }
                Speed: Single \{ singleTime < parallelTime ? "faster than" : "slower than" } Parallel
                """ );
        Set<String> set = new HashSet<>(parallelIndex.size());
        parallelIndex.forEach((x, y) -> {
            assertNotNull(simpleInvertedIndex.get(x));
            if (set.contains(x)) {
                System.err.println(parallelIndex);
                throw new AssertionError(STR. "'\{ x }' present two times" );
            }
            set.add(x);
        });
        assertEquals(simpleInvertedIndex.size(), parallelIndex.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {24, 8, 16})
    void testMapOfParallelIndex(int variant) throws InterruptedException {
        //given
        MasterNode master = new MasterNode();
        int fromIndex = Math.max(0, DEFAULT_PATHS.size() - 2);
        List<String> dataset = DEFAULT_PATHS.subList(fromIndex, DEFAULT_PATHS.size());
        var expectedIndex = master.buildIndexFromSource(dataset, 1, 1);
        var expectedList = expectedIndex.toList();
        InvertedIndex newIndex = master.buildIndexFromSource(dataset, variant, 1);
        List<Entry> newEntries = newIndex.toList();
        var parallelIndex = master.buildIndexFromSource(dataset, 1, 8);
        //when
        Thread parallelInsertionIntoIndex = new Thread(() -> {
            newEntries.stream().parallel().forEach(e -> parallelIndex.put(e.term(), e.postings()));
        });
        parallelInsertionIntoIndex.start();
        //then
        expectedList.stream().parallel()
                .forEach(e -> {
                    List<Posting> postings = parallelIndex.get(e.term());
                    assertTrue(postings.size() >= e.postings().size());//check getting old terms during update
                });
        parallelInsertionIntoIndex.join();
        newEntries.stream().parallel()
                .forEach(e -> {
                    List<Posting> postings = parallelIndex.get(e.term());
                    assertEquals(postings.size(), e.postings().size() + expectedIndex.get(e.term()).size());//check getting new terms after update
                });
    }
}