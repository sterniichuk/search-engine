package service;

import bootstrap.ClientRunner;
import domain.Query;
import domain.Response;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class QueryFactory {
    private final Random rand;

    public Query fromFile(File f) {
        try (var lines = Files.lines(f.toPath())) {
            String[] words = lines
                    .map(l -> l.split("\\s+"))
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
            int minWords = 7;
            int maxWords = 15;
            int phraseLength = rand.nextInt(maxWords - minWords + 1) + minWords;
            phraseLength = Math.min(phraseLength, words.length);
            int startIndex = rand.nextInt(words.length - phraseLength + 1);
            String text = Arrays.stream(words, startIndex, startIndex + phraseLength)
                    .collect(Collectors.joining(" "));
            String parent = f.getAbsolutePath().replace(File.separator + f.getName(), "");
            var response = new Response(parent, ClientRunner.fileNameToIndex(f.getName()));
            return new Query(text, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}