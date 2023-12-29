package bootstrap;

import domain.Statistic;
import service.ChartService;
import service.ProcessFactory;
import service.StatisticService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static config.Config.*;
import static domain.Mode.BUILDING;
import static domain.Mode.FULL;

public class Bootstrap {

    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            source, System.getProperty("user.dir"),
            variant, "-1",
            clientNumber, "32",
            queries, "50",
            mode, BUILDING.toString()
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
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-hh-mm-ss");

    public static void main(String[] args) {
        System.out.println("Java version: " + System.getProperty("java.version"));
        updateArguments(args, arguments);
        var builder = new ProcessFactory();
        String timeStamp = LocalDateTime.now().format(formatter);
        deleteOldStatistic();
        threadNumbers.forEach(threadNumber -> applicationRuntimeLifeCycle(threadNumber, builder));
        presentStats(timeStamp);
    }

    private static void applicationRuntimeLifeCycle(Integer threadNumber, ProcessFactory builder) {
        boolean serverStarted = false;
        try {
            System.out.println(STR. """
                    \n
                    Number of threads: \{ threadNumber }
                    Used arguments: \{ arguments.toString() }
                    """ );
            builder.exec(ServerRunner.class, List.of());//run server
            Thread.sleep(100);
            serverStarted = true;
            builder.exec(BuilderRunner.class, getBuilderArguments(threadNumber)).waitFor();//send request to build index
            if (FULL.toString().equals(arguments.get(mode))) {
                int numberOfClients = getIntValue(clientNumber, arguments);
                System.out.println("Start clients");
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
                System.out.println("Finish clients");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopServer(serverStarted, builder);
        }
    }

    public static void presentStats(String timeStamp) {
        var statisticService = new StatisticService();
        String currentVariant = arguments.get(variant);
        List<Statistic> statistics = statisticService.loadStatistic(currentVariant);
        var chartService = new ChartService();
        try {
            String filePath = statisticService.getFilePath(timeStamp) + STR. "var\{ currentVariant }.png" ;
            chartService.makeChart(statistics, STR. "Building inverted index. Variant:\{ currentVariant }" , filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteOldStatistic() {
        StatisticService service = new StatisticService();
        service.deleteStatisticFile(arguments.get(variant));
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

    private static List<String> getBuilderArguments(Integer threadNumber) {
        String variantValue = validInt(variant);
        return List.of(variant, variantValue, threads, threadNumber + "");
    }

    private static String validInt(String s) {
        int number = getIntValue(s, arguments);
        return number + "";
    }
}
