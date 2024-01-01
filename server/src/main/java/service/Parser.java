package service;

import domain.DocId;
import domain.TermDocIdPair;
import domain.Split;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class Parser {

    private static final int AVERAGE_WORD_LENGTH = 6;

    private final TextProcessor processor;


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
    public List<TermDocIdPair> map(List<Split> splits, Map<String, Integer> folders) {
        ArrayList<TermDocIdPair> result = new ArrayList<>();
        for (Split s : splits) {
            map(s, folders, result);
        }
        return result;
    }

    public List<TermDocIdPair> map(Split split, Map<String, Integer> folders) {
        ArrayList<TermDocIdPair> result = new ArrayList<>();
        map(split, folders, result);
        return result;
    }

    private void map(Split s, Map<String, Integer> folders, List<TermDocIdPair> result) {
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


    private List<TermDocIdPair> parseWords(File f, int fileId, byte folderId) {
        var list = new ArrayList<TermDocIdPair>(Math.max(1, (int) (f.length() / AVERAGE_WORD_LENGTH)));
        try (var br = new BufferedReader(new FileReader(f))) {
            int offset = 0;
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                var words = processor.processText(line);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    var keyValue = new TermDocIdPair(word, new DocId(folderId, fileId, (short) (offset + i)));
                    list.add(keyValue);
                }
                offset += words.length;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private int fileNameToIndex(String fileName) {
        String idAsString = fileName.substring(0, fileName.indexOf('_'));
        return Integer.parseInt(idAsString);
    }
}
