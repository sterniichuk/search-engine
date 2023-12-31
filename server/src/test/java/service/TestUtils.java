package service;

import java.io.File;
import java.util.List;

public class TestUtils {
    public static List<String> toAbsolute(List<String> l) {
        final String datasetPath = getPathToDataset();
        return l.stream().map(s -> datasetPath + File.separator + s).toList();
    }

    public static List<String> toAbsolute(String s) {
        final String datasetPath = getPathToDataset();
        return List.of(datasetPath + File.separator + s);
    }
    public static String source = new File(System.getProperty("indexdataset")).getAbsolutePath();

    public static String getPathToDataset() {
        return source;
    }
}
