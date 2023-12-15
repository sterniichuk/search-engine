package service;

import config.Config;
import domain.Segment;
import domain.TermDocIdPair;
import domain.Split;

import java.io.File;
import java.util.*;

public class MasterNode {
    public static final int READ_WHOLE_DATASET = -1;

    /**
     * @param paths   List of paths to files. aclImdb/*
     * @param variant Variant in the group of the student. -1 - when reading whole dataset
     */
    public InvertedIndex buildIndexFromSource(List<String> paths, int variant) {
        List<Split> splits = paths.stream().map(File::new).map((File x) -> toSplit(x, variant)).toList();
        Map<String, Integer> map = toMap(paths);
        Parser parser = new Parser(new TextProcessor());
        List<TermDocIdPair> pairs = parser.map(splits, map);
        Inverter inverter = new Inverter();
        inverter.reduce(List.of(new Segment(pairs)));
        return null;
    }

    private Map<String, Integer> toMap(List<String> paths) {
        Map<String, Integer> map = new HashMap<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            map.put(paths.get(i), i);
        }
        return Collections.unmodifiableMap(map);
    }

    private Split toSplit(File x, int variant) {
        String[] files = x.list();
        assert files != null : "No files in " + x;
        Arrays.sort(files);
        if (variant == READ_WHOLE_DATASET) {
            return new Split(files[0], files[files.length - 1]);
        }
        int start = (files.length / Config.VARIANT_DIVIDER) * (variant - 1);
        int finish = (files.length / Config.VARIANT_DIVIDER) * (variant);
        return new Split(files[start], files[finish]);
    }
}
