package service;

import domain.Statistic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class StatisticService {

    private final String folder = "./statistic";
    private final String $ = File.separator;
    private final String fileName = "measurements";

    public String getStatisticFileName(String variant, String timeStamp) {
        return STR. "\{ fileName }_var\{ variant }_time\{timeStamp}.csv" ;
    }

    public String getStatisticFileName(int variant, String timeStamp) {
        return getStatisticFileName(variant + "", timeStamp);
    }


    public void storeStatistic(Statistic stats, String timeStamp) {
        try {
            Path dir = Files.createDirectories(Path.of(folder));
            File file = new File(dir.toAbsolutePath() + $ + getStatisticFileName(stats.variant(), timeStamp));
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
            try (var out = new BufferedWriter(new FileWriter(file, true))) {
                out.write(STR. "\{ stats.threads() },\{ stats.time() },\{ stats.variant() }\n" );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Statistic> loadStatistic(String variant, String timeStamp) {
        try (var in = new BufferedReader(new FileReader(folder + $ + getStatisticFileName(variant, timeStamp)))) {
            return in.lines()
                    .map(s -> s.split(","))
                    .map(s -> Arrays.stream(s).mapToInt(Integer::parseInt).toArray())
                    .map(s -> new Statistic(s[1], s[2], s[0]))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Error loading statistics", e);
        }
    }

    public String getFilePath(String fileName) {
        return folder + $ + fileName;
    }

    public String getAbsoluteFolder() {
        return new File(folder).getAbsolutePath();
    }
}