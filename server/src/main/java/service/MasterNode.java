package service;

import domain.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MasterNode {
    private final FileSplitter splitter = new FileSplitter();
    private final StatisticService statistic = new StatisticService();

    /**
     * @param paths   List of paths to files. aclImdb/*
     * @param variant Variant in the group of the student. -1 - when reading whole dataset
     */
    public MasterResponse buildIndexFromSource(List<String> paths, int variant, int threadNumber) {
        Map<String, Integer> map = toMap(paths);
        InvertedIndex index;
        var start = System.nanoTime();
        if (threadNumber <= 1) {
            index = singleThreadIndexBuilding(paths, variant, map);
        } else {
            index = parallelIndexBuilding(paths, variant, map, threadNumber);
        }
        var time = System.nanoTime() - start;
        statistic.storeStatistic(new Statistic((int) (time / 1000_000), variant, threadNumber));
        Map<Integer, String> numberToFolder = invertMap(map);
        return new MasterResponse(index, numberToFolder);
    }

    private Map<String, Integer> toMap(List<String> paths) {
        Map<String, Integer> map = new HashMap<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            map.put(paths.get(i), i);
        }
        return Collections.unmodifiableMap(map);
    }

    private InvertedIndex singleThreadIndexBuilding(List<String> paths,
                                                    int variant,
                                                    Map<String, Integer> map) {
        List<Split> splits = paths.stream().map(File::new).map((File x) -> splitter.toSplit(x, variant)).toList();
        Parser parser = new Parser(new TextProcessor());
        List<TermDocIdPair> pairs = parser.map(splits, map);
        Inverter inverter = new Inverter();
        List<Entry> entries = inverter.reduce(List.of(new Segment(pairs)));
        InvertedIndex index = new SingleThreadInvertedIndex(entries.size());
        entries.forEach(x -> index.put(x.term(), x.postings()));
        return index;
    }

    private InvertedIndex parallelIndexBuilding(List<String> paths, int variant,
                                                Map<String, Integer> map, int threadNumber) {
        var splits = getSplits(paths, variant, threadNumber);
        final InvertedIndex[] index = new InvertedIndex[]{null};
        Parser parser = new Parser(new TextProcessor());
        Inverter inverter = new Inverter();
        List<? extends Future<?>> futures;
        try (var executor = Executors.newFixedThreadPool(threadNumber)) {
            futures = splits.stream()
                    .map(portionOfWork -> executor.submit(() -> {
                        try {
                            var pairs = parser.map(portionOfWork, map);
                            var reduce = inverter.reduce(List.of(new Segment(pairs)));
                            lazyInit(index, reduce);
                            reduce.forEach(e -> index[0].put(e.term(), e.postings()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).toList();
        }
        join(futures);
        return index[0];
    }

    private List<List<Split>> getSplits(List<String> paths, int variant, int threadNumber) {
        var splits = paths.stream().map(File::new)
                .flatMap((File x) -> splitter.toSplit(x, variant, threadNumber).stream()).toList();
        return splitter.packSplits(splits, threadNumber);
    }

    private static void lazyInit(InvertedIndex[] index, List<Entry> reduce) {
        if (index[0] == null) {
            synchronized (index) {
                if (index[0] == null) {
                    index[0] = new ConcurrentInvertedIndex(Math.max(reduce.size(), 32));
                }
            }
        }
    }

    private static void join(List<? extends Future<?>> futures) {
        futures.forEach(x -> {
            try {
                x.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Map<Integer, String> invertMap(Map<String, Integer> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
