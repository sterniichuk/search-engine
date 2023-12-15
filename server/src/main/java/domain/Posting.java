package domain;

public record Posting(byte folder, int docId, short position) implements Comparable<Posting> {
    @Override
    public int compareTo(Posting o) {
        if (folder != o.folder) {
            return folder - o.folder;
        } else if (docId != o.docId) {
            return docId - o.docId;
        }
        return position - o.position;
    }
}