package service;

import config.Config;
import domain.Split;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static config.Config.READ_WHOLE_DATASET;

public class FileSplitter {

    public Split toSplit(File x, int variant) {
        var numberOfFiles = checkFolder(x);
        if (variant == READ_WHOLE_DATASET) {
            return new Split(0, numberOfFiles, x.getPath());
        }
        int start = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant - 1);
        int finish = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant);
        return new Split(start, finish, x.getPath());
    }

    private int checkFolder(File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + file.getAbsolutePath());
        }
        String[] list = file.list();
        if (list == null) {
            throw new IllegalArgumentException("Problems with folder: " + file.getAbsolutePath());
        }
        return list.length;
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
                .mapToObj(i -> new Split(start + (i * step), start + ((i + 1) * step), folder))
                .collect(Collectors.toCollection(ArrayList::new));
        int last = start + (step * threadNumber);
        if (last < finish) {
            collect.add(new Split(last, finish, folder));
        }
        return collect;
    }

    public List<List<Split>> packSplits(List<Split> split, int threadNumber) {
        //noinspection unused
        List<List<Split>> result = IntStream.range(0, threadNumber)
                .mapToObj(i -> (List<Split>) (new ArrayList<Split>(split.size() / threadNumber + 1)))
                .toList();
        split.stream()
                .sorted(Comparator.comparingInt(Split::numberOfFiles).reversed())
                .forEach(s -> {
                    int minIndex = findMin(result);
                    result.get(minIndex).add(s);
                });
        return result;
    }

    private int findMin(List<List<Split>> result) {
        int min = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).isEmpty()) {
                return i;
            }
            var sum = result.get(i).stream().mapToInt(Split::numberOfFiles).sum();
            if (min > sum) {
                min = sum;
                index = i;
            }
        }
        return index;
    }
}