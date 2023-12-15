package config;

import java.util.List;

public interface Config {
    int VARIANT_DIVIDER = 50;

    List<String> DEFAULT_PATHS = List.of(
            "..\\datasets\\aclImdb\\test\\neg",
            "..\\datasets\\aclImdb\\test\\pos",
            "..\\datasets\\aclImdb\\train\\neg",
            "..\\datasets\\aclImdb\\train\\pos",
            "..\\datasets\\aclImdb\\train\\unsup"
    );
}
