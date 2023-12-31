package bootstrap;

import config.Config;
import controller.Builder;
import lombok.extern.slf4j.Slf4j;
import protocol.Request;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static config.Config.*;

@Slf4j
public class BuilderRunner {

    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            threads, "8",
            variant, "24",
            source, System.getProperty("user.dir"),
            output, System.getProperty("user.dir"),
            timeStamp, LocalDateTime.now().format(formatter)
    ));

    public static void main(String[] args) {
        updateArguments(args, arguments);
        int threadNumber = getIntValue(threads, arguments);
        int variant = getIntValue(Config.variant, arguments);
        var folder = new File(arguments.get(source));
        checkDirectory(folder);
        var folders = Config.DEFAULT_PATHS.stream()
                .map(s -> folder.getAbsolutePath() + File.separator + s)
                .toList();
        log.info(folders.toString());
        int code = (new Builder()).buildIndex(threadNumber, variant, folders, arguments.get(timeStamp), arguments.get(output));
        if (code != Request.CREATED) {
            log.info("Build index response: " + code);
        }
    }

    private static void checkDirectory(File folder) {
        boolean isValid = folder.exists() && folder.isDirectory()
                && folder.list() != null && Objects.requireNonNull(folder.list()).length > 0;
        if (!isValid) {
            throw new IllegalArgumentException("Bad path: " + arguments.get(source));
        }
    }
}