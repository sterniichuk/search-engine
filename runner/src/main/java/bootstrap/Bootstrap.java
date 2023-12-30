package bootstrap;

import config.Config;
import domain.Statistic;
import lombok.extern.slf4j.Slf4j;
import service.ChartService;
import service.ProcessFactory;
import service.StatisticService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static config.Config.*;
import static domain.Mode.BUILDING;
import static domain.Mode.FULL;

@Slf4j
public class Bootstrap {

    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            source, System.getProperty("user.dir"),
            variant, "24",
            clientNumber, "32",
            queries, "50",
            mode, BUILDING.toString(),
            iterations, "1",
            output, System.getProperty("user.dir")
    ));

    private static final int CPU_CORES = 8;
    private static final int CPU_LOGICAL_CORES = 16;
    private static final int MIN_THREADS = CPU_CORES / 2;

    private static final List<Integer> threadNumbers = List.of(
            1,
            MIN_THREADS,
            CPU_CORES,
            CPU_LOGICAL_CORES,
            CPU_LOGICAL_CORES * 2,
            CPU_LOGICAL_CORES * 4,
            CPU_LOGICAL_CORES * 8,
            CPU_LOGICAL_CORES * 16
    );

    public static void main(String[] args) throws InterruptedException {
        log.info("Java version: " + System.getProperty("java.version"));
        updateArguments(args, arguments);
        checkFolder();
        var builder = new ProcessFactory();
        int N = getIntValue(iterations, arguments);
        for (int i = 0; i < N; i++) {
            log.info(STR. "Iteration #\{ (i + 1) }" );
            String currentTimeStamp = LocalDateTime.now().format(formatter);
            threadNumbers.forEach(threadNumber -> applicationRuntimeLifeCycle(threadNumber, builder, currentTimeStamp));
            presentStats(currentTimeStamp);
            Thread.sleep(500);
        }
    }

    private static void checkFolder() {
        File folder = new File(arguments.get(source) + File.separator + "datasets");
        if (!folder.exists()) {
            throw new IllegalArgumentException(STR. "Folder doesn't exist. Path: \{ folder.getAbsolutePath() }" );
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException(STR. "Not a folder. Path: \{ folder.getAbsolutePath() }" );
        }
        String[] list = folder.list();
        if (list == null || list.length == 0) {
            throw new IllegalArgumentException(STR. "Source folde is empty. Path: \{ folder.getAbsolutePath() }" );
        }
    }

    private static void applicationRuntimeLifeCycle(Integer threadNumber, ProcessFactory builder, String timeStamp) {
        boolean serverStarted = false;
        try {
            log.info(STR. """

                    Number of threads: \{ threadNumber }
                    Used arguments: \{ arguments.toString() }
                    """ );
            builder.exec(ServerRunner.class, List.of());//run server
            Thread.sleep(100);
            serverStarted = true;
            builder.exec(BuilderRunner.class, getBuilderArguments(threadNumber, timeStamp)).waitFor();//send request to build index
            if (FULL.toString().equals(arguments.get(mode))) {
                int numberOfClients = getIntValue(clientNumber, arguments);
                log.info("Start clients");
                //noinspection unused
                List<Process> list = IntStream.range(0, numberOfClients)
                        .mapToObj(i -> {
                            try {
                                return builder.exec(ClientRunner.class, getClientArguments());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();//send search requests
                for (Process process : list) {
                    process.waitFor();//wait for all processes
                }
                log.info("Finish clients");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopServer(serverStarted, builder);
        }
    }

    public static void presentStats(String timeStamp) {
        var statisticService = new StatisticService(arguments.get(output));
        String currentVariant = arguments.get(variant);
        List<Statistic> statistics = statisticService.loadStatistic(currentVariant, timeStamp);
        var chartService = new ChartService();
        String filePath = statisticService.getFilePath(timeStamp) + STR. "var\{ currentVariant }.png" ;
        try {
            chartService.makeChart(statistics, STR. "Building inverted index. Variant:\{ currentVariant }" , filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info(STR. """

                Check statistic in the folder:\{ statisticService.getAbsoluteFolder() }
                Chart image:\{ new File(filePath).getAbsolutePath() }
                """ );
    }

    private static List<String> getClientArguments() {
        String variantValue = validInt(variant);
        String queriesValue = validInt(queries);
        String folder = arguments.get(source);
        return List.of(variant, variantValue, source, folder, queries, queriesValue);
    }

    private static void stopServer(boolean serverStarted, ProcessFactory builder) {
        if (serverStarted) {
            try {
                var killer = builder.exec(KillerRunner.class, List.of());
                killer.waitFor();//kill server and wait
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static List<String> getBuilderArguments(Integer threadNumber, String timeStamp) {
        String variantValue = validInt(variant);
        String sourceValue = arguments.get(source);
        return List.of(source, sourceValue,
                variant, variantValue,
                threads, threadNumber + "",
                Config.timeStamp, timeStamp,
                output, arguments.get(output));
    }

    private static String validInt(String s) {
        int number = getIntValue(s, arguments);
        return number + "";
    }
}
