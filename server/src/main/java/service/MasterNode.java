package service;

import domain.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class MasterNode {
    private final FileSplitter splitter = new FileSplitter();
    private final StatisticService statistic;

    public MasterNode(String outputFolder) {
        statistic = new StatisticService(outputFolder);
    }

    public MasterNode() {
        statistic = new StatisticService();
    }

    /**
     * @param paths     List of paths to files. aclImdb/*
     * @param variant   Variant in the group of the student. -1 - when reading whole dataset
     * @param timeStamp Time stamp used as an identifier for statistic files. If null, statistics will not be stored.
     */
    public MasterResponse buildIndexFromSource(List<String> paths, int variant, int threadNumber, String timeStamp) {
        Map<String, Integer> map = toMap(paths);
        InvertedIndex index;
        var start = System.nanoTime();
        if (threadNumber <= 1) {
            index = singleThreadIndexBuilding(paths, variant, map);
        } else {
            index = parallelIndexBuilding(paths, variant, map, threadNumber);
        }
        var time = System.nanoTime() - start;
        if (timeStamp != null) {
            statistic.storeStatistic(new Statistic((int) (time / 1000_000), variant, threadNumber), timeStamp);
        }
        Map<Integer, String> numberToFolder = invertMap(map);
        return new MasterResponse(index, numberToFolder);
    }

    public MasterResponse buildIndexFromSource(List<String> paths, int variant, int threadNumber) {
        return buildIndexFromSource(paths, variant, threadNumber, null);
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
        List<Entry> entries = inverter.reduce(pairs);
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
        try (var executor = ThreadPool.newFixedThreadPool(threadNumber)) {
            splits.forEach(portionOfWork -> executor.submit(() -> {
                        try {
                            var pairs = parser.map(portionOfWork, map);
                            var reduce = inverter.reduce(pairs);
                            lazyInit(index, reduce);
                            reduce.forEach(e -> index[0].put(e.term(), e.postings()));
                        } catch (Exception e) {
                            log.info(e.toString());
                        }
                    }));
        }
        return index[0];
    }

    private List<Split> getSplits(List<String> paths, int variant, int threadNumber) {
        return paths.stream().map(File::new)
                .flatMap((File x) -> splitter.toSplit(x, variant, threadNumber).stream()).toList();
    }

    @SuppressWarnings("all")
    private static void lazyInit(InvertedIndex[] index, List<Entry> reduce) {
        if (index[0] == null) {
            synchronized (index) {
                if (index[0] == null) {
                    index[0] = new ConcurrentInvertedIndex(Math.max(reduce.size(), 32));
                }
            }
        }
    }

    private Map<Integer, String> invertMap(Map<String, Integer> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
