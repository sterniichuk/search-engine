package config;

import java.util.List;
import java.util.Map;

public interface Config {
    int VARIANT_DIVIDER = 50;

    List<String> DEFAULT_PATHS = List.of(
            "..\\datasets\\aclImdb\\test\\neg",
            "..\\datasets\\aclImdb\\test\\pos",
            "..\\datasets\\aclImdb\\train\\neg",
            "..\\datasets\\aclImdb\\train\\pos",
            "..\\datasets\\aclImdb\\train\\unsup"
    );

    int serverPort = 8088;
    String host = "localhost";

    String clientNumber = "--clients";
    String source = "--source";
    String variant = "--variant";
    String threads = "--threads";
    String queries = "--queries";
    String mode = "--mode";
    String iterations = "--iterations";
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
