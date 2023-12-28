package bootstrap;

import config.Config;
import controller.Client;
import domain.QuerySplit;

import java.io.File;
import java.util.*;

import static config.Config.*;

public class ClientRunner {


    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            variant, "24",
            queries, "500",
            source, System.getProperty("user.dir")
    ));

    public static void main(String[] args) {
        updateArguments(args, arguments);
//        System.out.println(STR."Client arguments: \{arguments}");
        File[] files = getFiles();
        int numberOfQueries = getIntValue(queries, arguments);
        var client = new Client();
        var clientStatus = client.doQueries(numberOfQueries, files, false);
        int iterations = 5;
        for (int i = 0; i < iterations && clientStatus == Client.ClientStatus.BIND_EXCEPTION; i++) {
            int time = (new Random()).nextInt(250) + 50;
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            clientStatus = client.doQueries(numberOfQueries, files, i == iterations - 1);
        }
    }

    private static File[] getFiles() {
        int variant = getIntValue(Config.variant, arguments);
        var folder = new File(arguments.get(source));
        return Config.DEFAULT_PATHS.stream()
                .map(s -> folder.getAbsolutePath() + s.replace("..", ""))
                .map(File::new)
                .map(f -> toSplit(f, variant))
                .map(s -> {
                    List<File> files = new ArrayList<>(s.numberOfFiles());
                    for (var f : s.folder()) {
                        int index = fileNameToIndex(f.getName());
                        if (index < s.start() || index >= s.finish()) {
                            continue;
                        }
                        files.add(f);
                    }
                    return files;
                })
                .flatMap(List::stream)
                .toArray(File[]::new);
    }

    public static QuerySplit toSplit(File x, int variant) {
        File[] files = Objects.requireNonNull(x.listFiles());
        var numberOfFiles = files.length;
        if (variant == READ_WHOLE_DATASET) {
            return new QuerySplit(0, numberOfFiles - 1, files);
        }
        int start = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant - 1);
        int finish = (numberOfFiles / Config.VARIANT_DIVIDER) * (variant);
        return new QuerySplit(start, finish, files);
    }

    public static int fileNameToIndex(String fileName) {
        String idAsString = fileName.substring(0, fileName.indexOf('_'));
        return Integer.parseInt(idAsString);
    }
}
