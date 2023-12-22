package domain;

import java.util.List;
import java.util.Objects;

/**
 * Result of the reduce phase
 */
public record Posting(byte folder, int docId, List<Short> positions) implements Comparable<Posting> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posting posting = (Posting) o;
        return folder == posting.folder && docId == posting.docId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(folder, docId);
    }

    @Override
    public int compareTo(Posting o) {
        if (folder != o.folder) {
            return folder - o.folder;
        }
        return docId - o.docId;
    }
}