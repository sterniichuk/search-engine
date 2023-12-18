package domain;

import java.util.List;

/**
 * Result of the reduce phase
 */
public record Posting(byte folder, int docId, List<Short> positions) implements Comparable<Posting> {
    @Override
    public int compareTo(Posting o) {
        if (folder != o.folder) {
            return folder - o.folder;
        }
        return docId - o.docId;
    }
}