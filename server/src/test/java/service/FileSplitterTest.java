package service;

import domain.Split;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSplitterTest {
    private final String datasetPath = System.getProperty("indexdataset");

    @ParameterizedTest
    @ValueSource(ints = {3, 2, 5, 10})
    void toSplit(int threadNumber) {
        File folder = new File(datasetPath + "\\test\\neg");
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
    void parallelSplit(int threadNumber) {
        File folder = new File(datasetPath + "\\test\\neg");
        assert folder.exists();
        FileSplitter s = new FileSplitter();
        List<Split> split = s.toSplit(folder, 1, threadNumber);
        int actual = split.stream().mapToInt(Split::numberOfFiles).sum();
        var expected = s.toSplit(folder, 1).numberOfFiles();
        assertEquals(expected, actual);
    }
}