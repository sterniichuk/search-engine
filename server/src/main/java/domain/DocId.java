package domain;

public record DocId(byte folder, int docId, short position) implements Comparable<DocId> {
    @Override
    public int compareTo(DocId o) {
        if (folder != o.folder) {
            return folder - o.folder;
        } else if (docId != o.docId) {
            return docId - o.docId;
        }
        return position - o.position;
    }
}