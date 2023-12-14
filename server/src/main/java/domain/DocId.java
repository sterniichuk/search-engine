package domain;

public record DocId(byte folder, int id, short position) implements Comparable<DocId> {
    @Override
    public int compareTo(DocId o) {
        if (folder != o.folder) {
            return folder - o.folder;
        } else if (id != o.id) {
            return id - o.id;
        }
        return position - o.position;
    }
}