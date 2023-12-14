package domain;

public record KeyValue(String term, DocId docId) implements Comparable<KeyValue> {
    @Override
    public int compareTo(KeyValue o) {
        int termSort = this.term.compareTo(o.term);
        if (termSort != 0) {
            return termSort;
        }
        return this.docId.compareTo(o.docId);
    }
}