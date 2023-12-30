package service;

import domain.Entry;
import domain.Posting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static config.Config.DEFAULT_PATHS;
import static org.junit.jupiter.api.Assertions.*;
import static service.TestUtils.toAbsolute;

class MasterNodeTest {

    @Test
    void buildIndexFromSource() {
        MasterNode master = new MasterNode();
        var simpleInvertedIndex = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), 1, 1).index();
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
        int variant = 24;
        var simpleInvertedIndex = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), variant, 1).index();
        final long singleTime = System.currentTimeMillis() - start;
        //when
        start = System.currentTimeMillis();
        var parallelIndex = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), variant, threadNumber).index();
        final long parallelTime = System.currentTimeMillis() - start;
        //then
        System.out.println(STR. """
                Single thread time: \{ singleTime }
                \{ threadNumber } threads time: \{ parallelTime }
                Speed: Single \{ singleTime < parallelTime ? "faster than" : "slower than" } Parallel
                """ );
        Set<String> set = new HashSet<>(parallelIndex.size());
        //noinspection unused
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

//    @Test
    @SuppressWarnings("unused")
    void wholeDataset() {
        //given
        MasterNode master = new MasterNode();
        int variant = -1;
        //when
        List<String> defaultPaths = toAbsolute(DEFAULT_PATHS);
        var parallelIndex = master.buildIndexFromSource(defaultPaths, variant, 32).index();
        TextProcessor p = new TextProcessor();
        var dictionary = defaultPaths.stream()
                .map(i -> new File(i).listFiles())
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .parallel()
                .flatMap(file -> {
                    try {
                        return Files.lines(file.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).map(p::processText)
                .flatMap(Arrays::stream)
                .distinct().count();
        //then
        assertEquals(dictionary, parallelIndex.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {24, 8, 16})
    void testMapOfParallelIndex(int variant) throws InterruptedException {
        //given
        MasterNode master = new MasterNode();
        int fromIndex = Math.max(0, DEFAULT_PATHS.size() - 2);
        List<String> dataset = toAbsolute(DEFAULT_PATHS.subList(fromIndex, DEFAULT_PATHS.size()));
        var expectedIndex = master.buildIndexFromSource(dataset, 1, 1).index();
        var expectedList = expectedIndex.toList();
        InvertedIndex newIndex = master.buildIndexFromSource(dataset, variant, 1).index();
        List<Entry> newEntries = newIndex.toList();
        var parallelIndex = master.buildIndexFromSource(dataset, 1, 8).index();
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