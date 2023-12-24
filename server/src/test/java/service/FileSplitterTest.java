package service;

import config.Config;
import domain.Split;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSplitterTest {

    @ParameterizedTest
    @ValueSource(ints = {3, 2, 5, 10})
    void toSplit(int threadNumber) {
        File folder = new File("..\\datasets\\aclImdb\\test\\neg");
        assert folder.exists();
        FileSplitter s = new FileSplitter();
        List<Split> split = s.toSplit(folder, 1, threadNumber);
        for (int i = 0; i < split.size() - 1; i++) {
            assertEquals(split.get(i).finish(), split.get(i + 1).start());
        }
        assertEquals(250, split.getLast().finish());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 2, 5, 10})
    void toBalanceSplit(int threadNumber) {
        FileSplitter s = new FileSplitter();
        var split = Config.DEFAULT_PATHS.stream()
                .map(File::new).flatMap(f -> s.toSplit(f, 24, threadNumber).stream())
                .toList();
        List<List<Split>> balancedSplits = s.packSplits(split, threadNumber);
        double expectedMaxDiff = 0.2;
        int[] splitSize = balancedSplits.stream().mapToInt(i -> i.stream().mapToInt(Split::numberOfFiles).sum()).toArray();
        var min = Arrays.stream(splitSize).min().orElse(-1);
        var max = Arrays.stream(splitSize).max().orElse(-1);
        System.out.println(Arrays.toString(splitSize));
        System.out.println(STR."max=\{max}; min=\{min}");
        assertTrue(((double) (max - min) / max) < expectedMaxDiff);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 2, 5, 10})
    void parallelSplit(int threadNumber) {
        File folder = new File("..\\datasets\\aclImdb\\test\\neg");
        assert folder.exists();
        FileSplitter s = new FileSplitter();
        List<Split> split = s.toSplit(folder, 1, threadNumber);
        int actual = split.stream().mapToInt(Split::numberOfFiles).sum();
        var expected = s.toSplit(folder, 1).numberOfFiles();
        assertEquals(expected, actual);
    }
}