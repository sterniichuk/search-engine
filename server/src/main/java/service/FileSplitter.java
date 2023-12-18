package service;

import config.Config;
import domain.Split;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static service.MasterNode.READ_WHOLE_DATASET;

public class FileSplitter {

    public Split toSplit(File x, int variant) {
        var numberOfFiles = Objects.requireNonNull(x.listFiles()).length;
        if (variant == READ_WHOLE_DATASET) {
            return new Split(0, numberOfFiles - 1, x.getPath());
        }
        int start = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant - 1);
        int finish = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant);
        return new Split(start, finish, x.getPath());
    }

    public List<Split> toSplit(File x, int variant, int threadNumber) {
        var numberOfFiles = Objects.requireNonNull(x.listFiles()).length;
        if (variant == READ_WHOLE_DATASET) {
            return getSplits(0, numberOfFiles, threadNumber, x.getPath());
        }
        int start = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant - 1);
        int finish = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant);
        return getSplits(start, finish, threadNumber, x.getPath());
    }

    private static ArrayList<Split> getSplits(int start, int finish, int threadNumber, String folder) {
        int step = (finish - start + 1) / threadNumber;
        ArrayList<Split> collect = IntStream.range(0, threadNumber)
                .mapToObj(i -> new Split(i * step, (i + 1) * step, folder))
                .collect(Collectors.toCollection(ArrayList::new));
        int last = step * threadNumber;
        if (last < finish) {
            collect.add(new Split(last, finish, folder));
        }
        return collect;
    }
}