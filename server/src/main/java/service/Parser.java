package service;

import domain.DocId;
import domain.KeyValue;
import domain.Split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    /**
     * List of common suffixes
     */
    private static final String[] suffixes = {
            "s", "es", "ed", "ing", "ly", "er", "or", "ion", "able", "ible",
            "ment", "ness", "ful", "less", "al", "ive", "ize", "ify", "ise", "ist"
    };
    private static final int AVERAGE_WORD_LENGTH = 6;
    private static final int SMALL_WORD_LENGTH = 3;


    /**
     * Parses files defined by Split into a list of (term, docId) pairs.
     * <p>
     * Method performs the following steps:
     * <ol>
     *     <li>Reads files from folder defined by split.</li>
     *     <li>Performs text processing.</li>
     *     <li>Maps term to docId in the result.</li>
     * </ol>
     *
     * @param splits  list of splits. Each split contains a folder, start and finish index of files to be indexed.
     * @param folders map of folders and their ids.
     * @return list of (term, docId) pairs.
     */
    public List<KeyValue> map(List<Split> splits, Map<String, Integer> folders) {
        ArrayList<KeyValue> result = new ArrayList<>();
        for (Split split : splits) {
            var s = getSplitMapping(split);
            File folder = new File(s.folder());
            byte folderId = folders.get(s.folder()).byteValue();
            for (var f : Objects.requireNonNull(folder.listFiles())) {
                int index = fileNameToIndex(f.getName());
                if (index < s.start() || index >= s.finish()) {
                    continue;
                }
                result.addAll(parseWords(f, index, folderId));
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }


    private List<KeyValue> parseWords(File f, int fileId, byte folderId) {
        var list = new ArrayList<KeyValue>(Math.max(1, (int) (f.length() / AVERAGE_WORD_LENGTH)));
        try (var br = new BufferedReader(new FileReader(f))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                var words = textProcessing(line);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    var keyValue = new KeyValue(word, new DocId(folderId, fileId, (short) i));
                    list.add(keyValue);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private String[] textProcessing(String line) {
        var words = line.toLowerCase()
                .replaceAll("(?<=[a-z])\\.|'s", "")//Tokenization, Normalization
                .split("\\W+");
        return Arrays.stream(words).filter(s -> !s.isEmpty()).map(this::stemWord).toArray(String[]::new);//Stemming
    }


    private String stemWord(String word) {
        for (String suffix : suffixes) {
            if (word.length() > SMALL_WORD_LENGTH && word.length() > suffix.length() && word.endsWith(suffix)) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }

    private int fileNameToIndex(String fileName) {
        String idAsString = fileName.substring(0, fileName.indexOf('_'));
        return Integer.parseInt(idAsString);
    }

    record SplitMapping(String folder, int start, int finish) {
    }

    private SplitMapping getSplitMapping(Split split) {
        String folderPath = split.start().substring(0, split.start().lastIndexOf(File.separator));
        int startIndex = fileNameToIndex(split.start().replace(folderPath + File.separator, ""));
        int finishIndex = fileNameToIndex(split.finish().replace(folderPath + File.separator, ""));
        return new SplitMapping(folderPath, startIndex, finishIndex);
    }
}
