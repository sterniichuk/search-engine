package service;

import domain.Statistic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StatisticService {

    private final String folder = "../statistic";
    private final String $ = File.separator;
    private final String fileName = "measurements";


    public void storeStatistic(Statistic stats) {
        try {
            Path dir = Files.createDirectories(Path.of(folder));
            File file = new File(dir.toAbsolutePath() + $ + fileName);
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
            try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file, true))) {
                dataOutputStream.writeLong(stats.time());
                dataOutputStream.writeInt(stats.variant());
                dataOutputStream.writeInt(stats.threads());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Statistic> loadStatistic() {
        List<Statistic> statistics = new ArrayList<>();
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(folder + $ + fileName))) {
            while (dataInputStream.available() > 0) {
                long time = dataInputStream.readLong();
                int variant = dataInputStream.readInt();
                int threads = dataInputStream.readInt();
                statistics.add(new Statistic(time, variant, threads));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading statistics", e);
        }
        return statistics;
    }

    public void clearFolder() {
        File dir = new File(folder);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (var file : files) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}