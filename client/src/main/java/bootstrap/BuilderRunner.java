package bootstrap;

import config.Config;
import controller.Builder;
import protocol.Request;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static config.Config.*;

public class BuilderRunner {

    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            threads, "8",
            variant, "24",
            source, System.getProperty("user.dir")
    ));

    public static void main(String[] args) {
        updateArguments(args, arguments);
        int threadNumber = getIntValue(threads, arguments);
        int variant = getIntValue(Config.variant, arguments);
        var folder = new File(arguments.get(source));
        checkDirectory(folder);
        var folders = Config.DEFAULT_PATHS.stream().map(s -> folder.getAbsolutePath() + s.replace("..", "")).toList();
        System.out.println(folders);
        int code = (new Builder()).buildIndex(threadNumber, variant, folders);
        if (code != Request.CREATED) {
            System.err.println("Build index response: " + code);
        }
    }

    private static void checkDirectory(File folder) {
        boolean isValid = folder.exists() && folder.isDirectory() && folder.list() != null && folder.list().length > 0;
        if (!isValid) {
            throw new IllegalArgumentException("Bad path: " + arguments.get(source));
        }
    }
}
