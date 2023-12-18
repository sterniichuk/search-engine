package service;

import domain.Entry;
import domain.Segment;
import domain.Split;
import domain.TermDocIdPair;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MasterNode {
    public static final int READ_WHOLE_DATASET = -1;
    private final FileSplitter splitter = new FileSplitter();

    /**
     * @param paths   List of paths to files. aclImdb/*
     * @param variant Variant in the group of the student. -1 - when reading whole dataset
     */
    public InvertedIndex buildIndexFromSource(List<String> paths, int variant, int threadNumber) {
        Map<String, Integer> map = toMap(paths);
        InvertedIndex index;
        if (threadNumber <= 1) {
            index = singleThreadIndexBuilding(paths, variant, map);
        } else {
            index = parallelIndexBuilding(paths, variant, map, threadNumber);
        }
        return index;
    }

    private InvertedIndex parallelIndexBuilding(List<String> paths, int variant,
                                                Map<String, Integer> map, int threadNumber) {
        var splits = paths.stream().map(File::new)
                .flatMap((File x) -> splitter.toSplit(x, variant, threadNumber).stream()).toList();
        final InvertedIndex[] index = new InvertedIndex[]{null};
        Parser parser = new Parser(new TextProcessor());
        Inverter inverter = new Inverter();
        List<Future<?>> futures = new ArrayList<>();
        try (var executor = Executors.newFixedThreadPool(threadNumber)) {
            for (Split e1 : splits) {
                Future<?> f = executor.submit(() -> {
                    try {
                        var pairs = parser.map(List.of(e1), map);
                        var reduce = inverter.reduce(List.of(new Segment(pairs)));
                        if (index[0] == null) {
                            synchronized (index) {
                                if (index[0] == null) {
                                    index[0] = new ConcurrentInvertedIndex(reduce.size());
                                }
                            }
                        }
                        reduce.forEach(e -> index[0].put(e.term(), e.postings()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                futures.add(f);
            }
        }
        futures.forEach(x -> {
            try {
                x.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return index[0];
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

    private Map<String, Integer> toMap(List<String> paths) {
        Map<String, Integer> map = new HashMap<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            map.put(paths.get(i), i);
        }
        return Collections.unmodifiableMap(map);
    }
}
