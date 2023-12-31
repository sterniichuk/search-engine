package config;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public interface Config {
    int VARIANT_DIVIDER = 50;

    List<String> DEFAULT_PATHS = List.of(
            "test\\neg",
            "test\\pos",
            "train\\neg",
            "train\\pos",
            "train\\unsup"
    );

    int serverPort = 8088;
    String host = "localhost";

    String clients = "--clients";
    String source = "--source";
    String variant = "--variant";
    String threads = "--threads";
    String queries = "--queries";
    String mode = "--mode";
    String iterations = "--iterations";
    String timeStamp = "--time-stamp";
    String output = "--output";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-hh-mm-ss");

    int READ_WHOLE_DATASET = -1;


    static int getIntValue(String parameter, Map<String, String> args) {
        String value = args.get(parameter);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(STR. "Bad integer for parameter:\{ parameter }; value: \{ value }" );
        }
    }


    static void updateArguments(String[] args, Map<String, String> arguments) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException(STR."Some argument has missing value'");
        }
        for (int i = 0; i < args.length - 1; i += 2) {
            if (arguments.get(args[i]) == null) {
                throw new IllegalArgumentException(STR. "Unexpected argument:'\{ args[i] }'" );
            }
            arguments.put(args[i], args[i + 1]);
        }
    }
}